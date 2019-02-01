package io.iconator.monitor.service;

import io.iconator.commons.amqp.model.TokensAllocatedEmailMessage;
import io.iconator.commons.amqp.model.TransactionReceivedEmailMessage;
import io.iconator.commons.amqp.model.utils.MessageDTOHelper;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.db.services.EligibleForRefundService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.db.services.SaleTierService;
import io.iconator.commons.db.services.exception.PaymentLogNotFoundException;
import io.iconator.commons.db.services.exception.RefundEntryAlreadyExistsException;
import io.iconator.commons.model.db.EligibleForRefund;
import io.iconator.commons.model.db.EligibleForRefund.RefundReason;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.commons.model.db.PaymentLog.TransactionStatus;
import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.monitor.config.MonitorAppConfigHolder;
import io.iconator.monitor.service.exceptions.NoTierAtDateException;
import io.iconator.monitor.transaction.TransactionAdapter;
import io.iconator.monitor.transaction.exception.MissingTransactionInformationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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

    @Autowired
    private SaleTierRepository saleTierRepository;

    @Autowired
    private EligibleForRefundService eligibleForRefundService;

    @Autowired
    private PaymentLogService paymentLogService;

    @Autowired
    public SaleTierService saleTierService;

    @Autowired
    private MonitorAppConfigHolder appConfig;

    @Autowired
    private ICOnatorMessageService messageService;

    /**
     * Creates a refund entry for the given payment log. The created {@link EligibleForRefund} is added to the payment
     * log and the payment log's last processed date is updated. Starts a new transaction that is commited after leaving
     * this method. If any step in the process fails the whole transaction is rolledback. I.e. no refund entry is
     * created, the payment log does not reference any refund entry and the last processed date of the payment log is
     * not updated.
     * @param paymentLog The payment log for which to create a refund entry.
     * @param reason The refund reason.
     * @return the payment log updated with a reference to the created refund entry.
     * @throws RefundEntryAlreadyExistsException if a refund entry already exists for the given payment log.
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public PaymentLog createRefundEntryForPaymentLogAndCommit(
            PaymentLog paymentLog, RefundReason reason) throws RefundEntryAlreadyExistsException {

        paymentLog = createRefundEntryForAmount(paymentLog, reason,
                paymentLog.getCryptocurrencyAmount(), paymentLog.getUsdAmount());
        return paymentLogService.updateProcessedDateAndSave(paymentLog);
    }

    private PaymentLog createRefundEntryForAmount(
            PaymentLog paymentLog, RefundReason reason, BigInteger cryptocurrencyAmount, BigDecimal usdAmount)
            throws RefundEntryAlreadyExistsException {

        EligibleForRefund refund = eligibleForRefundService.save(
                new EligibleForRefund(reason, cryptocurrencyAmount, usdAmount,
                        paymentLog.getCurrency(), paymentLog.getInvestor(),
                        paymentLog.getTransactionId()));
        paymentLog.setEligibleForRefund(refund);
        return paymentLog;
    }

    private PaymentLog createRefundEntryForOverflow(PaymentLog paymentLog, BigDecimal overflowInUsd)
            throws RefundEntryAlreadyExistsException {

        BigInteger cryptocurrencyAmount = overflowInUsd.multiply(paymentLog.getUsdFxRate())
                .multiply(paymentLog.getCurrency().getAtomicUnitFactor())
                .toBigInteger();
        return createRefundEntryForAmount(paymentLog, RefundReason.TOKEN_OVERFLOW,
                cryptocurrencyAmount, overflowInUsd);
    }

    /**
     * Fetches the payment log corresponding to the given transaction id if it exists and is in status {@link TransactionStatus#PENDING}.
     * If successful, it updates the payment log's status to {@link TransactionStatus#BUILDING} and updates its last
     * processed date. Both changes are commited immediatly after the method ends.
     * @param transactionId The transaction id for which the corresponding payment log should be retrieved.
     * @return the payment log for given transaction id if it is in status {@link TransactionStatus#PENDING}.
     * Null otherwise.
     * @throws PaymentLogNotFoundException if no payment log exists for the given transaction id.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentLog getPendingPaymentLogForProcessing(String transactionId)
            throws PaymentLogNotFoundException {

        try {
            PaymentLog paymentLog = paymentLogService.getPaymentLog(transactionId);
            if (paymentLog.getTransactionStatus() != TransactionStatus.PENDING) {
                return null;
            } else {
                paymentLog.setTransactionStatus(TransactionStatus.BUILDING);
                return paymentLogService.updateProcessedDateAndSave(paymentLog);
            }
        } catch (OptimisticLockingFailureException e) {
            // If payment log was just now updated by another monitor instance
            // the payment log does not need to be reprocessed.
            return null;
        }
    }

    /**
     * Fetches the payment log that corresponds to the given transaction id if it exists, but only if the transaction
     * was previously incompletely processed in status {@link PaymentLog.TransactionStatus#BUILDING}.
     * If a payment log is returned, then its last processed date is updated and the change is commited immediately.
     * @param transactionId The transaction id for which corresponding payment log should be retrieved.
     * @return the payment log for given transaction id if the transaction has to be reprocessed
     * as a building transaction. Null, if the payment log is still in status {@link TransactionStatus#PENDING}, or the
     * transaction was already fully processed as a building transaction, or the transaction is still being processed at
     * the moment of calling this method. See the {@link io.iconator.monitor.config.MonitorAppConfigHolder#transactionProcessingTime}
     * property for the latter case.
     * @throws PaymentLogNotFoundException if no payment log exists for the given transaction id.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentLog getBuildingPaymentLogForReprocessing(String transactionId)
            throws PaymentLogNotFoundException {

        try {
            PaymentLog paymentLog = paymentLogService.getPaymentLog(transactionId);
            boolean pending = paymentLog.getTransactionStatus() == TransactionStatus.PENDING;
            boolean allocationMailSent = paymentLog.isAllocationMessageSent();
            boolean refundEntryExists = paymentLog.getEligibleForRefund() == null;
            // If a refund entry exists and no tokens have been allocated it means that the processing
            // of the transaction was completed with the refund entry. If some tokens where allocted
            // it means that there was an overflow in the token allocation but the transaction
            // was not fully processed if the allocation mail was not sent.
            boolean noTokensAllocated = paymentLog.getAllocatedTomics() == null;
            boolean recentlyChanged = paymentLog.getProcessedDate().getTime() >
                    new Date().getTime() - appConfig.getTransactionProcessingTime();
            if (pending || allocationMailSent || (refundEntryExists && noTokensAllocated)
                    || recentlyChanged) {
                return null;
            } else {
                // Updating the last processed date so that other monitor instances can see that
                // the transaction is currently being processed.
                return paymentLogService.updateProcessedDateAndSave(paymentLog);
            }
        } catch (OptimisticLockingFailureException e) {
            // If payment log was just now updated by another monitor instance
            // the payment log does not need to be reprocessed.
            return null;
        }
    }

    /**
     * Fetches the payment log that corresponds to the given transaction id if it exists, but only if the transaction
     * was incompletely processed in status {@link TransactionStatus#PENDING} before. If a payment log is returned, then
     * its last processed date is updated and committed immediately.
     * @param transactionId The transaction id for which corresponding payment log should be retrieved.
     * @return the payment log for given transaction id if the transaction has to be reprocessed as a pending
     * transaction. Null, if the payment log is already higher than the {@link TransactionStatus#PENDING} status, or the
     * transaction was already fully processed as a pending transaction, or the transaction is still being processed at
     * the moment of calling this method. See the {@link io.iconator.monitor.config.MonitorAppConfigHolder#transactionProcessingTime}
     * property for the latter case.
     * @throws PaymentLogNotFoundException if no payment log exists for the given transaction id.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentLog getPendingPaymentLogForReprocessing(String transactionId)
            throws PaymentLogNotFoundException {

        try {
            PaymentLog paymentLog = paymentLogService.getPaymentLog(transactionId);
            boolean confirmationMailSent = paymentLog.isTransactionReceivedMessageSent();
            boolean notPending = paymentLog.getTransactionStatus() != TransactionStatus.PENDING;
            boolean recentlyChanged = paymentLog.getProcessedDate().getTime() >
                    new Date().getTime() - appConfig.getTransactionProcessingTime();
            if (confirmationMailSent || notPending || recentlyChanged) {
                return null;
            } else {
                // Updating the last processed date so that other monitor instances can see that
                // the transaction is currently being processed.
                return paymentLogService.updateProcessedDateAndSave(paymentLog);
            }
        } catch (OptimisticLockingFailureException e) {
            // If payment log was just now updated by another monitor instance
            // the payment log does not need to be reprocessed.
            return null;
        }
    }

    /**
     * Creates, saves and immediately commits a new payment log for the given transaction.
     * @param tx The transaction for which to create a new payment log.
     * @param transactionStatus the status of the transaction/payment log.
     * @return the created payment log or null if the payment log could not be created.
     * @throws MissingTransactionInformationException if some transaction information cannot be retrieved from the
     * transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentLog createNewPaymentLog(TransactionAdapter tx,
                                           TransactionStatus transactionStatus)
            throws MissingTransactionInformationException {

        try {
            return paymentLogService.save(new PaymentLog(
                    tx.getTransactionId(), tx.getCurrencyType(),
                    transactionStatus));
        } catch (DataIntegrityViolationException e) {
            // The payment log was probably created by another instance just now.
            // This is not an error because the other instance will process the transaction.
            return null;
        }
    }

    /**
     * Queues a message for sending the "transaction received" email to the investor. Sets the corresponding flag on the
     * payment log and updates its last processed date. These changes are committed immediately.
     * @param paymentLog the payment log for which to send the message.
     * @param amountInMainUnit The invested amount in the main unit of the used cryptocurrency (e.g. in ETH or BC).
     * @param transactionUrl An URL to the transaction on a block explorer website.
     * @return The updated payment log.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentLog sendTransactionReceivedMessageAndSavePaymentLog(PaymentLog paymentLog, BigDecimal amountInMainUnit, String transactionUrl) {
        messageService.send(new TransactionReceivedEmailMessage(
                MessageDTOHelper.build(paymentLog.getInvestor()),
                amountInMainUnit,
                paymentLog.getCurrency(),
                transactionUrl));
        paymentLog.setTransactionReceivedMessageSent(true);
        return paymentLogService.updateProcessedDateAndSave(paymentLog);
    }

    /**
     * Queues a message for sending the "allocation" email to the investor. Sets the corresponding flag on the payment
     * log and updates its last processed date. These changes are committed immediately.
     * @param paymentLog the payment log for which to send the message.
     * @param amountInMainUnit The invested amount in the main unit of the used cryptocurrency (e.g. in ETH or BC).
     * @param transactionUrl An URL to the transaction on a block explorer website.
     * @return The updated payment log.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentLog sendAllocationMessageAndSavePaymentLog(PaymentLog paymentLog, BigDecimal amountInMainUnit, String transactionUrl) {
        messageService.send(new TokensAllocatedEmailMessage(
                MessageDTOHelper.build(paymentLog.getInvestor()),
                amountInMainUnit,
                paymentLog.getCurrency(),
                transactionUrl,
                convertTomicsToTokens(paymentLog.getAllocatedTomics())));
        paymentLog.setAllocationMessageSent(true);
        return paymentLogService.updateProcessedDateAndSave(paymentLog);
    }

    /**
     * Allocates tokens according to the USD amount in the given payment log.
     *
     * The selection of the tier happens based on the block time of the payment. If an 'old' payment
     * get processed with a block time that coincides with a tier that is not active anymore at
     * processing time, this method still tries to take the tokens from that tier.
     * @param paymentLog The payment for which tokens should be allocated.
     * @return The given input payment log with the amount of allocated tokens and/or a reference
     * to a refund entry set.
     * @throws NoTierAtDateException if the block time of the payment log does not fall into the
     * date range of any tier.
     */
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
            paymentLog.setAllocatedTomics(result.getAllocatedTomics());
            if (result.hasOverflow()) {
                LOG.info("{} transaction {} generated a overflow of {} USD", paymentLog.getCurrency().name(), paymentLog.getTransactionId(), result.getOverflow());
                try {
                    paymentLog = createRefundEntryForOverflow(paymentLog, result.getOverflow());
                } catch (RefundEntryAlreadyExistsException e) {
                    LOG.error("Couldn't save overflow USD {} as refund beacause a refund entry for " +
                            "transaction {} already existed", result.getOverflow(), paymentLog.getTransactionId());
                }
            }
            return paymentLogService.updateProcessedDateAndSave(paymentLog);
        } else {
            throw new NoTierAtDateException();
        }
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
        BigDecimal discountedUsdPerToken = appConfig.getFiatBasePerToken().multiply(BigDecimal.ONE.subtract(discount));
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
        BigDecimal discountedUsdPerToken = appConfig.getFiatBasePerToken().multiply(BigDecimal.ONE.subtract(discount));
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
            tier.setEndDate(tier.getStartDate());
        } else {
            dateShift = tier.getEndDate().getTime() - blockTime.getTime();
            tier.setEndDate(blockTime);
        }
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

