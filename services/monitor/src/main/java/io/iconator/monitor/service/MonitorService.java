package io.iconator.monitor.service;

import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.db.services.SaleTierService;
import io.iconator.commons.db.services.exception.PaymentLogNotFoundException;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.EligibleForRefund;
import io.iconator.commons.model.db.EligibleForRefund.RefundReason;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.EligibleForRefundRepository;
import io.iconator.commons.sql.dao.PaymentLogRepository;
import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.monitor.config.MonitorAppConfig;
import io.iconator.monitor.service.exceptions.NoTierAtDateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Date;
import java.util.Optional;

@Service
public class MonitorService {

    private final static Logger LOG = LoggerFactory.getLogger(MonitorService.class);

    private final static long MINUTE_IN_MS = 60 * 1000;

    @Autowired
    private SaleTierRepository saleTierRepository;

    @Autowired
    private EligibleForRefundRepository eligibleForRefundRepository;

    @Autowired
    private PaymentLogRepository paymentLogRepository;

    @Autowired
    private PaymentLogService paymentLogService;

    @Autowired
    public SaleTierService saleTierService;

    @Autowired
    private MonitorAppConfig appConfig;

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public EligibleForRefund createRefundEntryForPaymentLogAndCommit(
            PaymentLog paymentLog, RefundReason reason) {
        return createRefundEntryForAmount(paymentLog, reason, paymentLog.getPaymentAmount());
    }

    private EligibleForRefund createRefundEntryForAmount(
            PaymentLog paymentLog, RefundReason reason, BigInteger amount) {

        if (paymentLog.getTxIdentifier() == null) {
            throw new IllegalArgumentException("Transaction Id of PaymentLog must not be null.");
        }
        EligibleForRefund refundEntry = new EligibleForRefund(
                reason, amount, paymentLog.getCurrency(),
                paymentLog.getInvestorId(), paymentLog.getTxIdentifier());

        if (eligibleForRefundRepository.existsByTxIdentifierAndCurrency(
                refundEntry.getTxIdentifier(), refundEntry.getCurrency())) {
            throw new IllegalArgumentException("Refund entry must not already exist in database.");
        }
        refundEntry = eligibleForRefundRepository.saveAndFlush(refundEntry);
        paymentLog.setEligibleForRefundId(refundEntry.getId());
        paymentLogRepository.saveAndFlush(paymentLog);
        LOG.info("Saving new refund entry for {} transaction {}.", paymentLog.getCurrency().name(), paymentLog.getTxIdentifier());
        return refundEntry;
    }

    private EligibleForRefund createRefundEntryForOverflow(PaymentLog paymentLog, BigDecimal overflow) {
        BigInteger amount = overflow.multiply(paymentLog.getUsdFxRate())
                .multiply(paymentLog.getCurrency().getAtomicUnitFactor())
                .toBigInteger();
        return createRefundEntryForAmount(paymentLog, RefundReason.TOKEN_OVERFLOW, amount);
    }

    /**
     * Retrieves and returns the PaymentLog with the given transaction id and
     * currency if it exists and needs to be reprocessed. In that case it resets
     * the creation date to the currrent date. If it does not exist a new one is
     * created.
     *
     * @param txId     the id of the transaction for which the Payment log should
     *                 be fetched/created.
     * @param currency the currency of the transaction for which the Payment log
     *                 should be fetched/created.
     * @return the retrieved or created PaymentLog. Or null if the transaction
     * does not need to be processed.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public PaymentLog getOrCreatePaymentLog(String txId, CurrencyType currency) {
        PaymentLog paymentLog;
        try {
            paymentLog = paymentLogService.getPaymentLogReadOnly(txId, currency);
            if (paymentLog.wasFullyProcessed()) return null;
            if (paymentLog.wasCreatedRecently(MINUTE_IN_MS)) return null;
            LOG.info("Found payment which was not fully processed. Resetting creation date and restarting processing over again.");
            paymentLog = paymentLogService.getPaymentLogForUpdate(txId, currency);
            paymentLog.setCreateDate(new Date());
        } catch (PaymentLogNotFoundException e) {
            paymentLog = new PaymentLog(txId, new Date(), currency);
        }
        return paymentLogService.save(paymentLog);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public TokenAllocationResult allocateTokens(PaymentLog paymentLog)
            throws NoTierAtDateException {

        Date blockTime = paymentLog.getBlockDate();
        BigDecimal usd = paymentLog.getUsdValue();
        Optional<SaleTier> oTier = saleTierService.getTierAtDate(blockTime);
        if (oTier.isPresent()) {
            handleDynamicMax(oTier.get());
            TokenAllocationResult result;
            if (oTier.get().isFull()) {
                result = distributeToNextTier(usd, oTier, blockTime);
            } else {
                result = distributeToTier(usd, oTier.get(), blockTime);
            }
            updatePaymentLog(paymentLog, result);
            if (result.hasOverflow()) {
                LOG.info("{} transaction {} generated a overflow of {} USD", paymentLog.getCurrency().name(), paymentLog.getTxIdentifier(), result.getOverflow());
                createRefundEntryForOverflow(paymentLog, result.getOverflow());
            }
            return result;
        } else {
            throw new NoTierAtDateException();
        }
    }

    private void updatePaymentLog(PaymentLog paymentLog, TokenAllocationResult result) {

        paymentLog.setTomicsAmount(result.getAllocatedTomics());
        paymentLogRepository.saveAndFlush(paymentLog);
    }

    private TokenAllocationResult distributeToTier(BigDecimal usd, SaleTier tier, Date blockTime) {
        // Remembering decimal value to have more precision in case a conversion back to usd is necessary because of an overflow.
        BigDecimal tomicsDecimal = convertUsdToTomics(usd, tier.getDiscount());
        BigInteger tomicsInteger = tomicsDecimal.toBigInteger();
        if (tier.isAmountOverflowingTier(tomicsInteger)) {
            BigInteger remainingTomicsOnTier = tier.getRemainingTomics();
            BigDecimal overflowOverTier = tomicsDecimal.subtract(new BigDecimal(remainingTomicsOnTier));
            BigDecimal overflowInUsd = convertTomicsToUsd(overflowOverTier, tier.getDiscount());
            if (isOverflowingTotalMax(remainingTomicsOnTier)) {
                LOG.debug("Distributing {} USD to tier {} lead to overflow over the total " +
                        "available amount of tokens.", usd, tier.getTierNo());
                return handleTotalMaxOverflow(tier, remainingTomicsOnTier).addToOverflow(overflowInUsd);
            } else {
                tier.setTomicsSold(tier.getTomicsMax());
                tier.setTomicsSold(tier.getTomicsMax());
                tier = saleTierRepository.save(tier);
                if (tier.hasDynamicDuration()) shiftDates(tier, blockTime);
                LOG.debug("Distributing {} USD to tier {}. Distributing Overflow of {} USD to " +
                        "next tier.", usd, tier.getTierNo(), overflowInUsd);
                return distributeToNextTier(overflowInUsd, saleTierService.getSubsequentTier(tier), blockTime)
                        .addToAllocatedTomics(remainingTomicsOnTier);
            }
        } else {
            if (isOverflowingTotalMax(tomicsInteger)) {
                LOG.debug("Distributing {} USD to tier {} lead to overflow over the total " +
                        "available amount of tokens.", usd, tier.getTierNo());
                return handleTotalMaxOverflow(tier, tomicsInteger);
            } else {
                tier.setTomicsSold(tier.getTomicsSold().add(tomicsInteger));
                if (tier.isFull()) {
                    if (tier.hasDynamicDuration()) shiftDates(tier, blockTime);
                    saleTierService.getSubsequentTier(tier).ifPresent(this::handleDynamicMax);
                }
                LOG.debug("{} tomics distributed to tier {}", tomicsInteger, tier.getTierNo());
                saleTierRepository.save(tier);
                return new TokenAllocationResult(tomicsInteger, BigDecimal.ZERO);
            }
        }
    }

    /**
     * @param usd      the USD amount to convert to tokens.
     * @param discount the discount applied to the price of a token. E.g. with 0.25 the price for
     *                 one token is reduced to 75% of the original price.
     * @return the amount of tokens (in their atomic unit) worth the given USD amount
     */
    public BigDecimal convertUsdToTomics(BigDecimal usd, BigDecimal discount) {
        BigDecimal discountedUsdPerToken = appConfig.getUsdPerToken().multiply(BigDecimal.ONE.subtract(discount));
        BigDecimal tokens = usd.divide(discountedUsdPerToken, MathContext.DECIMAL128);
        return convertTokensToTomics(tokens);
    }

    /**
     * @param tomics   the amount of tokens to convert given in their atomic unit.
     * @param discount the discount applied to the price of a token. E.g. with 0.25 the price for
     *                 one token is reduced to 75% of the original price.
     * @return the price in USD for the given amount of atomic tokens.
     */
    public BigDecimal convertTomicsToUsd(BigDecimal tomics, BigDecimal discount) {
        BigDecimal tokens = convertTomicsToTokens(tomics);
        BigDecimal discountedUsdPerToken = appConfig.getUsdPerToken().multiply(BigDecimal.ONE.subtract(discount));
        return tokens.multiply(discountedUsdPerToken);
    }

    /**
     * @param tomics   the amount of tokens to convert given in their atomic unit.
     * @param discount the discount applied to the price of a token. E.g. with 0.25 the price for
     *                 one token is reduced to 75% of the original price.
     * @return the price in USD for the given amount of atomic tokens.
     */
    public BigDecimal convertTomicsToUsd(BigInteger tomics, BigDecimal discount) {
        return convertTomicsToUsd(new BigDecimal(tomics), discount);
    }

    public BigDecimal convertTomicsToTokens(BigInteger tomics) {
        return new BigDecimal(tomics.divide(appConfig.getAtomicUnitFactor()));
    }

    public BigDecimal convertTomicsToTokens(BigDecimal tomics) {
        return tomics.divide(new BigDecimal(appConfig.getAtomicUnitFactor()), MathContext.DECIMAL128);
    }

    public BigDecimal convertTokensToTomics(BigDecimal value) {
        return value.multiply(new BigDecimal(appConfig.getAtomicUnitFactor()));
    }

    private TokenAllocationResult handleTotalMaxOverflow(SaleTier tier, BigInteger tomicsForTier) {
        TokenAllocationResult result = distributeTotalRemainingTokensToTier(tier);
        BigInteger totalMaxOverflow = tomicsForTier.subtract(result.getAllocatedTomics());
        return result.addToOverflow(convertTomicsToUsd(totalMaxOverflow, tier.getDiscount()));
    }

    private TokenAllocationResult distributeTotalRemainingTokensToTier(SaleTier tier) {
        BigInteger totalRemainingTokens = getTotalRemainingTomics();
        tier.setTomicsSold(tier.getTomicsSold().add(totalRemainingTokens));
        saleTierRepository.save(tier);
        return new TokenAllocationResult(totalRemainingTokens, BigDecimal.ZERO);
    }

    private BigInteger getTotalRemainingTomics() {
        return getTotalTomicsAmount().subtract(saleTierService.getTotalTomicsSold());
    }

    private boolean isOverflowingTotalMax(BigInteger tomics) {
        return tomics.compareTo(getTotalRemainingTomics()) > 0;
    }

    private void shiftDates(SaleTier tier, Date blockTime) {
        long dateShift = tier.getEndDate().getTime() - blockTime.getTime();
        tier.setEndDate(blockTime);
        tier = saleTierRepository.save(tier);
        saleTierService.getAllSubsequentTiers(tier).forEach(t -> {
            t.setStartDate(new Date(t.getStartDate().getTime() - dateShift));
            t.setEndDate(new Date(t.getEndDate().getTime() - dateShift));
            saleTierRepository.save(t);
        });
    }

    private TokenAllocationResult distributeToNextTier(BigDecimal usd, Optional<SaleTier> oTier, Date blockTime) {
        if (oTier.isPresent()) {
            handleDynamicMax(oTier.get());
            return distributeToTier(usd, oTier.get(), blockTime);
        } else {
            return new TokenAllocationResult(BigInteger.ZERO, usd);
        }
    }

    private void handleDynamicMax(SaleTier tier) {
        if (tier.hasDynamicMax() && (tier.getTomicsMax().compareTo(BigInteger.ZERO) == 0
                || tier.getTomicsMax() == null)) {
            tier.setTomicsMax(getTotalRemainingTomics());
            saleTierRepository.save(tier);
        }
    }

    private BigInteger getTotalTomicsAmount() {
        return convertTokensToTomics(appConfig.getTotalTokenAmount())
                .toBigInteger();
    }

    /**
     * Holds the result from a token allocation.
     * The tomics are the amount of allocated tokens in atomic units.
     * The overflow is the amount of USD which could not be converted and
     * allocated due to limited tier capacity.
     */
    public static class TokenAllocationResult {

        private BigInteger tomics;
        private BigDecimal overflow;

        public TokenAllocationResult(BigInteger tomics, BigDecimal overflow) {
            this.tomics = tomics;
            this.overflow = overflow;
        }

        public TokenAllocationResult() {
            this.tomics = BigInteger.ZERO;
            this.overflow = BigDecimal.ZERO;
        }

        public boolean hasOverflow() {
            return overflow.compareTo(BigDecimal.ZERO) > 0;
        }

        /**
         * @return the overflow from an token allocation in USD.
         */
        public BigDecimal getOverflow() {
            return overflow;
        }

        /**
         * @return the amount of allocated tokens in atomic units.
         */
        public BigInteger getAllocatedTomics() {
            return tomics;
        }

        public TokenAllocationResult addToAllocatedTomics(BigInteger tomics) {
            this.tomics = this.tomics.add(tomics);
            return this;
        }

        public TokenAllocationResult addToOverflow(BigDecimal overflow) {
            this.overflow = this.overflow.add(overflow);
            return this;
        }
    }
}

