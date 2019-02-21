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

/**
 * Contains the transaction processing logic that should be used by all blockchain-specific
 * monitor implementations (e.g. {@link EthereumMonitor}.
 */
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

    /**
     * Starts the monitoring and transaction processing.
     * Also starts transmitting {@link io.iconator.commons.amqp.model.BlockNrMessage}s, containing
     * the latest block numbers, to a message queue.
     */
    protected abstract void start() throws Exception;

    /**
     * Adds the given address to the set of monitored addresses.
     * The given address creation time can be used by implementing classes to determine at which
     * block the monitoring of the blockchain should start. Obviously, blocks that are older than
     * the date of registration of the first investor do not have to be scanned.
     * @param address                  payment address which will be monitored
     * @param addressCreationTimestamp Creation time of the address in miliseconds since the epoch.
     */
    abstract protected void addPaymentAddressesForMonitoring(String address, Long addressCreationTimestamp);

    /**
     * @param receivingAddress the address for which to check if it is monitored by this monitor.
     * @return ture if the given address is monitored. False, otherwise.
     */
    abstract protected boolean isAddressMonitored(String receivingAddress);

    /**
     * Entry point to the processing flow of transactions that are in status pending, i.e. are not
     * on a block yet but were seen in the network.
     * This must be called by blockchain-specific implementations when a new pending transaction to
     * a monitored address is seen in the network.
     * @param tx The pending transaction.
     */
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

    /**
     * Entry point to the processing flow of transactions that are in status building, i.e. are on
     * a block already.
     * This must be called by blockchain-specific implementations when a transaction to a monitored
     * address has been added to a block. Transactions do not have to be covered by more blocks
     * before calling this method. If, in the end, they should not be on the main chain, they will
     * never be marked as confirmed transactions.
     * @param tx The building transaction.
     */
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

    /**
     * Updates the given payment log (in status {@link PaymentLog.TransactionStatus#BUILDING})
     * according to the given transaction. If all neccessary information can be retrieved, including
     * the exchange rate, the payment log is updated and the changes are immediatly commited.
     * If some information cannot be retrieved a refund entry is created (also immediatly commited)
     * and the payment log is not updated.
     * @param tx The transaction corresponding to the payment log.
     * @param paymentLog The payment log to update.
     * @return the update payment log or null if some transaction information could not be retrieved
     * and a refund entry had to be created.
     * @throws RefundEntryAlreadyExistsException if a refund entry already exists for this payment
     * log. A refund entry is create if some transaction information cannot be retrieved.
     */
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
        return paymentLogService.saveRequireNewTransaction(paymentLog);
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

    /**
     * Entry point to the token allocation.
     * A retryer is used because exceptions might arise due to optimistic locking when trying to
     * make changes to the sale tiers.
     * This is only package-private so that it can be called in unit tests directly.
     * @param paymentLog The payment log of the transaction for which to allocate tokens.
     * @return The given payment log updated with the allocated amount of tokens if the allocation
     * was successful.
     * @throws Throwable If an error occured when allocating the tokens. Though, before exiting this
     * method a refund entry is created.
     */
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
            LOG.error("Failed to distribute payment to tiers for {} transaction {}.",
                    paymentLog.getCurrency().name(), paymentLog.getTransactionId(), e.getCause());
            RefundReason reason = RefundReason.TOKEN_ALLOCATION_FAILED;
            monitorService.createRefundEntryForPaymentLogAndCommit(paymentLog, reason);
            throw e;
        }
    }

    /**
     * Gets the payment log corresponding to the given transaction and set its status to confirmed.
     * This method should be called by blockchain-specific implementations when a transaction to a
     * monitored address can be confired, i.e. is burried under enough blocks.
     * @param tx The confirmed transaction.
     */
    protected void confirmTransaction(TransactionAdapter tx) {
        try {
            LOG.info("Setting status of transaction {} to confirmed.", tx.getTransactionId());
            PaymentLog paymentLog = paymentLogService.getPaymentLog(tx.getTransactionId());
            paymentLog.setTransactionStatus(TransactionStatus.CONFIRMED);
            paymentLogService.saveRequireNewTransaction(paymentLog);
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
