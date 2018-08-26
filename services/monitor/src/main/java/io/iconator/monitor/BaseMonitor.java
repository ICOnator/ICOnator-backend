package io.iconator.monitor;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import io.iconator.commons.amqp.model.BlockNrMessage;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.db.services.InvestorService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.db.services.exception.PaymentLogNotFoundException;
import io.iconator.commons.db.services.exception.RefundEntryAlradyExistsException;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.EligibleForRefund.RefundReason;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.commons.model.db.PaymentLog.TransactionStatus;
import io.iconator.monitor.config.MonitorAppConfig;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.MonitorService;
import io.iconator.monitor.transaction.TransactionAdapter;
import io.iconator.monitor.transaction.exception.MissingTransactionInformationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import javax.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.github.rholder.retry.WaitStrategies.randomWait;

@Component
abstract public class BaseMonitor {

    private final static Logger LOG = LoggerFactory.getLogger(BaseMonitor.class);

    protected final MonitorService monitorService;
    protected final PaymentLogService paymentLogService;
    protected final FxService fxService;
    protected final InvestorService investorService;
    protected final ICOnatorMessageService messageService;

    @Autowired
    private MonitorAppConfig appConfig;

    public BaseMonitor(MonitorService monitorService,
                       PaymentLogService paymentLogService,
                       FxService fxService,
                       ICOnatorMessageService messageService,
                       InvestorService investorService) {
        this.monitorService = monitorService;
        this.paymentLogService = paymentLogService;
        this.fxService = fxService;
        this.messageService = messageService;
        this.investorService = investorService;
    }

//    protected void start() {
//        startMonitoringBlocks(this::processBlockNrMessage);
//        startMonitoringPendingTransactions(this::processPendingTransactions);
//        startMonitoringBuildingTransactions(this::processBuildingTransaction);
//    }

    /**
     * @param receivingAddress the address to which a payment has been made.
     * @return true if this address is monitored. False otherwise.
     */
    abstract protected boolean isAddressMonitored(String receivingAddress);

    protected abstract void startMonitoringBlocks(Consumer<BlockNrMessage> blockNrMessageConsumer);

    protected abstract void startMonitoringPendingTransactions(Consumer<TransactionAdapter> pendingTransactionConsumer);

    protected abstract void startMonitoringBuildingTransactions(Consumer<TransactionAdapter> buildingTransactionConsumer);

    protected abstract void startMonitoringTransactionUntilConfirmed(TransactionAdapter tx);

    protected void processBlockNrMessage(BlockNrMessage blockNrMessage) {
        messageService.send(blockNrMessage);
    }

    protected void processPendingTransactions(TransactionAdapter tx) {
        try {
            if (!isAddressMonitored(tx.getReceivingAddress())) return;
            PaymentLog paymentLog;
            try {
                paymentLog = monitorService.getPendingPaymentLogForReprocessing(tx.getTransactionId());
            } catch (PaymentLogNotFoundException e) {
                paymentLog = createNewPaymentLog(tx);
            }
            if (paymentLog == null) return;
            paymentLog.setCryptocurrencyAmount(tx.getTransactionValue());
            paymentLog.setInvestor(tx.getAssociatedInvestor());
            monitorService.sendTransactionReceivedMessageAndSavePaymentLog(
                    paymentLog, tx.getTransactionValueInMainUnit(),
                    tx.getTransactionUrl());
        } catch (MissingTransactionInformationException e) {
            LOG.error("Error processing transaction", e);
        }
    }

    private PaymentLog createNewPaymentLog(TransactionAdapter tx)
            throws MissingTransactionInformationException {

        try {
            return paymentLogService.saveAndCommit(new PaymentLog(
                    tx.getTransactionId(), new Date(), tx.getCurrencyType(),
                    TransactionStatus.PENDING));
        } catch (DataIntegrityViolationException e) {
            // The payment log was probably created by another instance just now.
            // This is not an error because the other instance will process the transaction.
            return null;
        }
    }

    protected void processBuildingTransaction(TransactionAdapter tx) {
        try {
            if (!isAddressMonitored(tx.getReceivingAddress())) return;
            PaymentLog paymentLog;
            try {
                paymentLog = monitorService.getPendingPaymentLogForProcessing(
                        tx.getTransactionId());
                if (paymentLog == null) {
                    paymentLog = monitorService.getBuildingPaymentLogForReprocessing(
                            tx.getTransactionId());
                }
            } catch (PaymentLogNotFoundException e) {
                paymentLog = createNewPaymentLog(tx);
            }
            // Update attributes of payment log if it has been newly created but also if it was
            // fetched for reprocessing.
            paymentLog = updateBuildingPaymentLog(tx, paymentLog);
            if (paymentLog == null) return; // no processing required.
            BigInteger allocatedTomics = paymentLog.getAllocatedTomics();
            if (allocatedTomics == null) {
                // Tokens have not yet been allocated because the payment log is new or the last
                // time it was processed the processing stopped unexpectedly before the allocation.
                paymentLog = allocateTokensWithRetries(paymentLog);
            }
            monitorService.sendAllocationMessageAndSavePaymentLog(
                    paymentLog, tx.getTransactionValueInMainUnit(), tx.getTransactionUrl());
            LOG.info("Transaction processed: {} {} / {} USD / {} FX / investor id {} / Time: {} / Tomics Amount {}",
                    tx.getTransactionValueInMainUnit(), tx.getCurrencyType().name(), paymentLog.getUsdValue(),
                    paymentLog.getUsdFxRate(), tx.getAssociatedInvestor(), paymentLog.getCreateDate(),
                    paymentLog.getAllocatedTomics());

        } catch (Throwable t) {
            LOG.error("Error processing transaction.", t);
        }
    }

    private PaymentLog updateBuildingPaymentLog(TransactionAdapter tx, PaymentLog paymentLog) {
        if (paymentLog == null) return null;
        RefundReason reason = null;
        try {
            reason = RefundReason.TRANSACTION_VALUE_MISSING;
            paymentLog.setCryptocurrencyAmount(tx.getTransactionValue());
            BigDecimal valueInMainUnit = tx.getTransactionValueInMainUnit();

            reason = RefundReason.INVESTOR_MISSING;
            paymentLog.setInvestor(tx.getAssociatedInvestor());

            reason = RefundReason.BLOCK_TIME_MISSING;
            paymentLog.setBlockTime(tx.getBlockTime());

            reason = RefundReason.FX_RATE_MISSING;
            BigDecimal fxRate = getUSDExchangeRate(tx.getBlockTime().toInstant(), tx.getCurrencyType());
            paymentLog.setUsdFxRate(fxRate);
            paymentLog.setUsdValue(valueInMainUnit.multiply(fxRate));

        } catch (MissingTransactionInformationException e) {
            try {
                monitorService.createRefundEntryForPaymentLogAndCommit(paymentLog, reason);
            } catch (RefundEntryAlradyExistsException re) {
                LOG.error("Couldn't save refund entry for transction {} because one alrady existed " +
                        "for that transaction.", paymentLog.getTransactionId());
            }
            return null;
        }
        return paymentLogService.saveAndCommit(paymentLog);
    }

    private BigDecimal getUSDExchangeRate(Instant blockTimestamp, CurrencyType currencyType)
            throws MissingTransactionInformationException {

        try {
            Optional<BigDecimal> USDExchangeRate = fxService.getUSDExchangeRate(blockTimestamp, currencyType);
            return USDExchangeRate.orElseThrow(() -> new NoSuchElementException());
        } catch (Exception e) {
            throw new MissingTransactionInformationException("Couldn't fetch USD to " + currencyType.name() + " exchange rate.", e);
        }

    }

    public PaymentLog allocateTokensWithRetries(PaymentLog paymentLog)
            throws Throwable {

        if (paymentLog.getUsdValue() == null) {
            throw new IllegalArgumentException("PaymentLog's amount in USD must not be null.");
        }
        if (paymentLog.getBlockTime() == null) {
            throw new IllegalArgumentException("PaymentLog's block time must not be null.");
        }

        LOG.debug("Calling token allocation with {} USD for {} transaction {}.",
                paymentLog.getUsdValue().toPlainString(), paymentLog.getCurrency(),
                paymentLog.getTransactionId());

        // Retry as long as there are database locking exceptions.
        Retryer<PaymentLog> retryer =
                RetryerBuilder.<PaymentLog>newBuilder()
                        .retryIfExceptionOfType(OptimisticLockingFailureException.class)
                        .retryIfExceptionOfType(OptimisticLockException.class)
                        .withWaitStrategy(randomWait(
                                appConfig.getTokenConversionMaxTimeWait(), TimeUnit.MILLISECONDS))
                        .withStopStrategy(StopStrategies.neverStop())
                        .build();

        try {
            PaymentLog updatedPaymentLog = retryer.call(
                    () -> monitorService.allocateTokens(paymentLog));

            LOG.debug("Allocated {} tomics for {} transaction {}.",
                    updatedPaymentLog.getAllocatedTomics(), updatedPaymentLog.getCurrency(),
                    updatedPaymentLog.getTransactionId());

            return paymentLog;
        } catch (Throwable e) {
            LOG.error("Failed to distribute payment to tiers for {} transaction {}.", paymentLog.getCurrency().name(), paymentLog.getTransactionId(), e.getCause());
            RefundReason reason = RefundReason.TOKEN_ALLOCATION_FAILED;
            monitorService.createRefundEntryForPaymentLogAndCommit(paymentLog, reason);
            throw e;
        }
    }
}
