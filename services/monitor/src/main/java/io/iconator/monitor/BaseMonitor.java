package io.iconator.monitor;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import io.iconator.commons.amqp.model.BlockNrMessage;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.db.services.InvestorService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.db.services.exception.PaymentLogNotFoundException;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.EligibleForRefund.RefundReason;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.commons.model.db.PaymentLog.TransactionStatus;
import io.iconator.monitor.config.MonitorAppConfig;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.MonitorService;
import io.iconator.monitor.service.MonitorService.TokenAllocationResult;
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
import java.util.Date;
import java.util.NoSuchElementException;
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
            monitorService.sendTransactionSeenMailAndSavePaymentLog(
                    paymentLog, tx.getTransactionValueInMainUnit());
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
            if (paymentLog == null) return; // no processing required.
            if (paymentLog.getAllocatedTomics() == null) {
                // TODO the token allocation has to be repeated. I.e. do the whole processing.
            } else {
                // TODO only the allocation email has to be send.
            }
            paymentLog = updateBuildingPaymentLog(tx, paymentLog);
            if (paymentLog == null) return;
            LOG.debug("Calling token allocation with {} USD for {} transaction {}.", paymentLog.getUsdValue().toPlainString(), paymentLog.getCurrency(), paymentLog.getTransactionId());
            TokenAllocationResult result;
            result = allocateTokensWithRetries(paymentLog);
            LOG.debug("Allocated {} tomics for {} transaction {}.", result.getAllocatedTomics(), tx.getCurrencyType().name(), tx.getTransactionId());
            BigInteger tomics = result.getAllocatedTomics();
            LOG.debug("For {} USD {} atomic tokens where allocated. {} transaction {}.", paymentLog.getUsdValue().toPlainString(), tomics, tx.getCurrencyType().name(), tx.getTransactionId());
            monitorService.sendAllocationMailAndSavePaymentLog(
                    paymentLog, tx.getTransactionValueInMainUnit(), tx.getWebLinkToTransaction());
            LOG.info("Transaction processed: {} {} / {} USD / {} FX / investor id {} / Time: {} / Tomics Amount {}",
                    tx.getTransactionValueInMainUnit(), tx.getCurrencyType().name(), paymentLog.getUsdValue(),
                    paymentLog.getUsdFxRate(), tx.getAssociatedInvestor(), paymentLog.getCreateDate(),
                    paymentLog.getAllocatedTomics());

        } catch (Throwable t) {
            LOG.error("Error processing transaction.", t);
        }
    }

    private PaymentLog updateBuildingPaymentLog(TransactionAdapter tx, PaymentLog paymentLog) {
        RefundReason reason = null;
        try {
            reason = RefundReason.TRANSACTION_VALUE_MISSING;
            paymentLog.setCryptocurrencyAmount(tx.getTransactionValue());
            BigDecimal valueInMainUnit = tx.getTransactionValueInMainUnit();

            reason = RefundReason.INVESTOR_MISSING;
            paymentLog.setInvestor(tx.getAssociatedInvestor());

            reason = RefundReason.BLOCK_TIME_MISSING;
            paymentLog.setBlockTime(tx.getBlockTime());

            reason = RefundReason.BLOCK_HEIGHT_MISSING;
            long blockHeight = tx.getBlockHeight();

            reason = RefundReason.FX_RATE_MISSING;
            BigDecimal fxRate = getUSDExchangeRate(blockHeight, tx.getCurrencyType());
            paymentLog.setUsdFxRate(fxRate);
            paymentLog.setUsdValue(valueInMainUnit.multiply(fxRate));

        } catch (MissingTransactionInformationException e) {
            monitorService.createRefundEntryForPaymentLogAndCommit(paymentLog, reason);
            return null;
        }
        return paymentLogService.saveAndCommit(paymentLog);
    }

    private BigDecimal getUSDExchangeRate(long blockHeight, CurrencyType currencyType)
            throws MissingTransactionInformationException {

        try {
            BigDecimal USDExchangeRate = fxService.getUSDExchangeRate(blockHeight, currencyType);
            if (USDExchangeRate == null) throw new NoSuchElementException();
            return USDExchangeRate;
        } catch (Exception e) {
            throw new MissingTransactionInformationException("Couldn't fetch USD to " + currencyType.name() + " exchange rate.", e);
        }
    }

    public TokenAllocationResult allocateTokensWithRetries(PaymentLog paymentLog)
            throws Throwable {

        if (paymentLog.getUsdValue() == null) {
            throw new IllegalArgumentException("PaymentLog's amount in USD must not be null.");
        }
        if (paymentLog.getBlockTime() == null) {
            throw new IllegalArgumentException("PaymentLog's block time must not be null.");
        }

        // Retry as long as there are database locking exceptions.
        Retryer<TokenAllocationResult> retryer =
                RetryerBuilder.<MonitorService.TokenAllocationResult>newBuilder()
                        .retryIfExceptionOfType(OptimisticLockingFailureException.class)
                        .retryIfExceptionOfType(OptimisticLockException.class)
                        .withWaitStrategy(randomWait(
                                appConfig.getTokenConversionMaxTimeWait(), TimeUnit.MILLISECONDS))
                        .withStopStrategy(StopStrategies.neverStop())
                        .build();

        try {
            TokenAllocationResult result = retryer.call(
                    () -> monitorService.allocateTokens(paymentLog));
            if (result == null) throw new NoSuchElementException();
            return result;
        } catch (Throwable e) {
            LOG.error("Failed to distribute payment to tiers for {} transaction {}.", paymentLog.getCurrency().name(), paymentLog.getTransactionId(), e.getCause());
            RefundReason reason = RefundReason.CONVERSION_TO_TOKENS_FAILED;
            monitorService.createRefundEntryForPaymentLogAndCommit(paymentLog, reason);
            throw e;
        }
    }

}
