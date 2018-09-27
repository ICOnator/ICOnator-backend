package io.iconator.monitor;

import com.github.rholder.retry.Retryer;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.db.services.InvestorService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.db.services.exception.PaymentLogNotFoundException;
import io.iconator.commons.db.services.exception.RefundEntryAlreadyExistsException;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.EligibleForRefund.RefundReason;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.commons.model.db.PaymentLog.TransactionStatus;
import io.iconator.monitor.config.MonitorAppConfigHolder;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.MonitorService;
import io.iconator.monitor.transaction.TransactionAdapter;
import io.iconator.monitor.transaction.exception.MissingTransactionInformationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.github.rholder.retry.WaitStrategies.randomWait;

@Component
abstract public class BaseMonitor {

    private final static Logger LOG = LoggerFactory.getLogger(BaseMonitor.class);

    protected final MonitorService monitorService;
    protected final PaymentLogService paymentLogService;
    protected final FxService fxService;
    protected final InvestorService investorService;
    protected final ICOnatorMessageService messageService;
    protected final MonitorAppConfigHolder configHolder;
    protected final Retryer<PaymentLog> retryer;

    public BaseMonitor(MonitorService monitorService,
                       PaymentLogService paymentLogService,
                       FxService fxService,
                       ICOnatorMessageService messageService,
                       InvestorService investorService,
                       MonitorAppConfigHolder configHolder,
                       Retryer retryer) {
        this.monitorService = monitorService;
        this.paymentLogService = paymentLogService;
        this.fxService = fxService;
        this.messageService = messageService;
        this.investorService = investorService;
        this.configHolder = configHolder;
        this.retryer = retryer;
    }

    protected abstract void start() throws Exception;

    /**
     * @param address                  payment address which will be monitored
     * @param addressCreationTimestamp Creation time of the address in miliseconds since the epoch.
     *                                 Some blockchain implementations can use this to determine
     *                                 from which block to start scanning the chain.
     */
    abstract protected void addPaymentAddressesForMonitoring(String address, Long addressCreationTimestamp);

    /**
     * @param receivingAddress the address to which a payment has been made.
     * @return TRUE if this address is monitored. False otherwise.
     */
    abstract protected boolean isAddressMonitored(String receivingAddress);

    protected void processPendingTransactions(TransactionAdapter tx) {
        try {
            if (!isAddressMonitored(tx.getReceivingAddress())) return;
            PaymentLog paymentLog;
            try {
                paymentLog = monitorService.getPendingPaymentLogForReprocessing(tx.getTransactionId());
            } catch (PaymentLogNotFoundException e) {
                paymentLog = monitorService.createNewPaymentLog(tx, TransactionStatus.PENDING);
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

    protected void processBuildingTransaction(TransactionAdapter tx) {
        PaymentLog paymentLog = null;
        try {
            if (!isAddressMonitored(tx.getReceivingAddress())) return;
            try {
                paymentLog = monitorService.getPendingPaymentLogForProcessing(
                        tx.getTransactionId());
                if (paymentLog == null) {
                    paymentLog = monitorService.getBuildingPaymentLogForReprocessing(
                            tx.getTransactionId());
                }
            } catch (PaymentLogNotFoundException e) {
                paymentLog = monitorService.createNewPaymentLog(tx, TransactionStatus.BUILDING);
            }
            paymentLog = updateBuildingPaymentLog(tx, paymentLog);
            if (paymentLog == null) return; // no processing required.

            if (isAmountInsufficient(paymentLog.getUsdAmount())) {
                monitorService.createRefundEntryForPaymentLogAndCommit(paymentLog,
                        RefundReason.INSUFFICIENT_PAYMENT_AMOUNT);
                return;
            }

            BigInteger allocatedTomics = paymentLog.getAllocatedTomics();
            if (allocatedTomics == null) {
                // Tokens have not yet been allocated because the payment log is new or the last
                // time it was processed the processing stopped unexpectedly before the allocation.
                paymentLog = allocateTokensWithRetries(paymentLog);
            }
            monitorService.sendAllocationMessageAndSavePaymentLog(
                    paymentLog, tx.getTransactionValueInMainUnit(), tx.getTransactionUrl());
            LOG.info("Transaction processed: {} {} / {} USD / {} FX / investor id {} / Time: {} / Tomics Amount {}",
                    tx.getTransactionValueInMainUnit(), tx.getCurrencyType().name(), paymentLog.getUsdAmount(),
                    paymentLog.getUsdFxRate(), tx.getAssociatedInvestor(), paymentLog.getCreateDate(),
                    paymentLog.getAllocatedTomics());

        } catch (RefundEntryAlreadyExistsException e) {
            LOG.error("Couldn't save refund entry for transction {} because one already existed " +
                    "for that transaction.", paymentLog.getTransactionId(), e);
        } catch (Throwable t) {
            LOG.error("Error processing transaction.", t);
        }
    }

    private boolean isAmountInsufficient(BigDecimal usdAmount) {
        return usdAmount.compareTo(configHolder.getFiatBasePaymentMinimum()) < 0;
    }

    private PaymentLog updateBuildingPaymentLog(TransactionAdapter tx, PaymentLog paymentLog) throws RefundEntryAlreadyExistsException {
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
            paymentLog.setUsdAmount(valueInMainUnit.multiply(fxRate));

        } catch (MissingTransactionInformationException e) {
            monitorService.createRefundEntryForPaymentLogAndCommit(paymentLog, reason);
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

    PaymentLog allocateTokensWithRetries(PaymentLog paymentLog)
            throws Throwable {

        if (paymentLog.getUsdAmount() == null) {
            throw new IllegalArgumentException("PaymentLog's amount in USD must not be null.");
        }
        if (paymentLog.getBlockTime() == null) {
            throw new IllegalArgumentException("PaymentLog's block time must not be null.");
        }

        LOG.debug("Calling token allocation with {} USD for {} transaction {}.",
                paymentLog.getUsdAmount().toPlainString(), paymentLog.getCurrency(),
                paymentLog.getTransactionId());


        try {
            // Retry as long as there are database locking exceptions.
            PaymentLog updatedPaymentLog = retryer.call(
                    () -> monitorService.allocateTokens(paymentLog));

            LOG.debug("Allocated {} tomics for {} transaction {}.",
                    updatedPaymentLog.getAllocatedTomics(), updatedPaymentLog.getCurrency(),
                    updatedPaymentLog.getTransactionId());

            return updatedPaymentLog;
        } catch (Throwable e) {
            LOG.error("Failed to distribute payment to tiers for {} transaction {}.", paymentLog.getCurrency().name(), paymentLog.getTransactionId(), e.getCause());
            RefundReason reason = RefundReason.TOKEN_ALLOCATION_FAILED;
            monitorService.createRefundEntryForPaymentLogAndCommit(paymentLog, reason);
            throw e;
        }
    }

    protected void confirmTransaction(TransactionAdapter tx) {
        try {
            LOG.info("Setting status of transaction {} to confirmed.", tx.getTransactionId());
            PaymentLog paymentLog = paymentLogService.getPaymentLog(tx.getTransactionId());
            paymentLog.setTransactionStatus(TransactionStatus.CONFIRMED);
            paymentLogService.saveAndCommit(paymentLog);
        } catch (MissingTransactionInformationException e) {
            LOG.error("Couldn't set payment log status to confirmed because the transaction id " +
                    "could not be retrieved from the {} transaction.", tx.getCurrencyType(), e);
        } catch (PaymentLogNotFoundException e) {
            try {
                LOG.error("No payment log existed for {} transaction {} when trying to set the " +
                        "transaction status to CONFIRMED", tx.getCurrencyType(), tx.getTransactionId(), e);
            } catch (MissingTransactionInformationException ignore) {
            }
        }
    }

}
