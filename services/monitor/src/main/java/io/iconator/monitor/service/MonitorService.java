package io.iconator.monitor.service;

import io.iconator.commons.amqp.model.TokensAllocatedEmailMessage;
import io.iconator.commons.amqp.model.TransactionReceivedEmailMessage;
import io.iconator.commons.amqp.model.utils.MessageDTOHelper;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.db.services.EligibleForRefundService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.db.services.SaleTierService;
import io.iconator.commons.db.services.exception.PaymentLogNotFoundException;
import io.iconator.commons.db.services.exception.RefundEntryAlradyExistsException;
import io.iconator.commons.model.db.EligibleForRefund;
import io.iconator.commons.model.db.EligibleForRefund.RefundReason;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.commons.model.db.PaymentLog.TransactionStatus;
import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.PaymentLogRepository;
import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.monitor.config.MonitorAppConfigHolder;
import io.iconator.monitor.service.exceptions.NoTierAtDateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
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
    private EligibleForRefundService eligibleForRefundService;

    @Autowired
    private PaymentLogRepository paymentLogRepository;

    @Autowired
    private PaymentLogService paymentLogService;

    @Autowired
    public SaleTierService saleTierService;

    @Autowired
    private MonitorAppConfigHolder appConfig;

    @Autowired
    private ICOnatorMessageService messageService;

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public PaymentLog createRefundEntryForPaymentLogAndCommit(
            PaymentLog paymentLog, RefundReason reason) throws RefundEntryAlradyExistsException {
        return createRefundEntryForAmount(paymentLog, reason, paymentLog.getCryptocurrencyAmount(), paymentLog.getUsdAmount());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public PaymentLog getPendingPaymentLogForProcessing(String transactionId)
            throws PaymentLogNotFoundException {

        try {
            PaymentLog paymentLog = paymentLogService.getPaymentLog(transactionId);
            if (paymentLog.getTransactionStatus() != TransactionStatus.PENDING) {
                return null;
            } else {
                paymentLog.setCreateDate(new Date());
                paymentLog.setTransactionStatus(TransactionStatus.BUILDING);
                return paymentLogService.save(paymentLog);
            }
        } catch (OptimisticLockingFailureException e) {
            // If payment log was just now updated by another monitor instance
            // the payment log does not need to be reprocessed.
            return null;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public PaymentLog getBuildingPaymentLogForReprocessing(String transactionId)
            throws PaymentLogNotFoundException {

        try {
            PaymentLog paymentLog = paymentLogService.getPaymentLog(transactionId);
            boolean pending = paymentLog.getTransactionStatus() == TransactionStatus.PENDING;
            boolean allocationMailSent = paymentLog.isAllocationMailSent();
            boolean refundEntryExists = paymentLog.getEligibleForRefund() == null;
            boolean noTokensAllocated = paymentLog.getAllocatedTomics() == null;
            boolean youngerThanOneMinute =
                    paymentLog.getCreateDate().getTime() > new Date().getTime() - MINUTE_IN_MS;
            if (pending || allocationMailSent || (refundEntryExists && noTokensAllocated)
                    || youngerThanOneMinute) {
                return null;
            } else {
                paymentLog.setCreateDate(new Date());
                // Not updating the transaction status. It is either BUIDLING or
                // CONFIRMED which is both fine at this point.
                return paymentLogService.save(paymentLog);
            }
        } catch (OptimisticLockingFailureException e) {
            // If payment log was just now updated by another monitor instance
            // the payment log does not need to be reprocessed.
            return null;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public PaymentLog getPendingPaymentLogForReprocessing(String transactionId)
            throws PaymentLogNotFoundException {

        try {
            PaymentLog paymentLog = paymentLogService.getPaymentLog(transactionId);
            boolean confirmationMailSent = paymentLog.isConfirmationMailSent();
            boolean notPending = paymentLog.getTransactionStatus() != TransactionStatus.PENDING;
            boolean youngerThanOneMinute =
                    paymentLog.getCreateDate().getTime() > new Date().getTime() - MINUTE_IN_MS;
            if (confirmationMailSent || notPending || youngerThanOneMinute) {
                return null;
            } else {
                paymentLog.setCreateDate(new Date());
                return paymentLogService.save(paymentLog);
            }
        } catch (OptimisticLockingFailureException e) {
            // If payment log was just now updated by another monitor instance
            // the payment log does not need to be reprocessed.
            return null;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void sendTransactionReceivedMessageAndSavePaymentLog(PaymentLog paymentLog, BigDecimal amountInMainUnit, String transactionUrl) {
        messageService.send(new TransactionReceivedEmailMessage(
                MessageDTOHelper.build(paymentLog.getInvestor()),
                amountInMainUnit,
                paymentLog.getCurrency(),
                transactionUrl));
        paymentLog.setConfirmationMailSent(true);
        paymentLogService.save(paymentLog);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void sendAllocationMessageAndSavePaymentLog(PaymentLog paymentLog, BigDecimal amountInMainUnit, String transactionUrl) {
        messageService.send(new TokensAllocatedEmailMessage(
                MessageDTOHelper.build(paymentLog.getInvestor()),
                amountInMainUnit,
                paymentLog.getCurrency(),
                transactionUrl,
                convertTomicsToTokens(paymentLog.getAllocatedTomics())));
        paymentLog.setAllocationMailSent(true);
        paymentLogService.save(paymentLog);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public PaymentLog allocateTokens(PaymentLog paymentLog)
            throws NoTierAtDateException {

        Date blockTime = paymentLog.getBlockTime();
        BigDecimal usd = paymentLog.getUsdAmount();
        Optional<SaleTier> oTier = saleTierService.getTierAtDate(blockTime);
        if (oTier.isPresent()) {
            handleDynamicMax(oTier.get());
            TokenAllocationResult result;
            if (oTier.get().isFull()) {
                result = distributeToNextTier(usd, oTier, blockTime);
            } else {
                result = distributeToTier(usd, oTier.get(), blockTime);
            }
            paymentLog = updatePaymentLog(paymentLog, result);
            if (result.hasOverflow()) {
                LOG.info("{} transaction {} generated a overflow of {} USD", paymentLog.getCurrency().name(), paymentLog.getTransactionId(), result.getOverflow());
                try {
                    createRefundEntryForOverflow(paymentLog, result.getOverflow());
                } catch (RefundEntryAlradyExistsException e) {
                    LOG.error("Couldn't save overflow USD {} as refund beacause a refund entry for " +
                            "transaction {} already existed", result.getOverflow(), paymentLog.getTransactionId());
                }
            }
            return paymentLog;
        } else {
            throw new NoTierAtDateException();
        }
    }

    private PaymentLog createRefundEntryForAmount(
            PaymentLog paymentLog, RefundReason reason, BigInteger cryptocurrencyAmount, BigDecimal usdAmount)
            throws RefundEntryAlradyExistsException {

        EligibleForRefund refund = eligibleForRefundService.save(
                new EligibleForRefund(reason, cryptocurrencyAmount, usdAmount,
                        paymentLog.getCurrency(), paymentLog.getInvestor(),
                        paymentLog.getTransactionId()));
        paymentLog.setEligibleForRefund(refund);
        return paymentLogRepository.saveAndFlush(paymentLog);
    }

    private PaymentLog createRefundEntryForOverflow(PaymentLog paymentLog, BigDecimal overflowInUsd)
            throws RefundEntryAlradyExistsException {

        BigInteger cryptocurrencyAmount = overflowInUsd.multiply(paymentLog.getUsdFxRate())
                .multiply(paymentLog.getCurrency().getAtomicUnitFactor())
                .toBigInteger();
        return createRefundEntryForAmount(paymentLog, RefundReason.TOKEN_OVERFLOW,
                cryptocurrencyAmount, overflowInUsd);
    }

    private PaymentLog updatePaymentLog(PaymentLog paymentLog, TokenAllocationResult result) {
        paymentLog.setAllocatedTomics(result.getAllocatedTomics());
        return paymentLogRepository.saveAndFlush(paymentLog);
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
        long dateShift;
        if (blockTime.getTime() < tier.getStartDate().getTime()) {
            dateShift = tier.getEndDate().getTime() - tier.getStartDate().getTime();
        } else {
            dateShift = tier.getEndDate().getTime() - blockTime.getTime();
        }
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
    private static class TokenAllocationResult {

        private BigInteger tomics;
        private BigDecimal overflow;

        TokenAllocationResult(BigInteger tomics, BigDecimal overflow) {
            this.tomics = tomics;
            this.overflow = overflow;
        }

        boolean hasOverflow() {
            return overflow.compareTo(BigDecimal.ZERO) > 0;
        }

        /**
         * @return the overflow from a token allocation in USD.
         */
        BigDecimal getOverflow() {
            return overflow;
        }

        /**
         * @return the amount of allocated tokens in atomic units.
         */
        BigInteger getAllocatedTomics() {
            return tomics;
        }

        TokenAllocationResult addToAllocatedTomics(BigInteger tomics) {
            this.tomics = this.tomics.add(tomics);
            return this;
        }

        TokenAllocationResult addToOverflow(BigDecimal overflow) {
            this.overflow = this.overflow.add(overflow);
            return this;
        }
    }
}

