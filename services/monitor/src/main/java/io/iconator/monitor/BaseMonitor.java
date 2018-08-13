package io.iconator.monitor;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import io.iconator.commons.amqp.model.FundsReceivedEmailMessage;
import io.iconator.commons.amqp.model.utils.MessageDTOHelper;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.EligibleForRefund;
import io.iconator.commons.model.db.EligibleForRefund.RefundReason;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.monitor.config.MonitorAppConfig;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.MonitorService;
import io.iconator.monitor.service.MonitorService.TokenAllocationResult;
import io.iconator.monitor.service.exceptions.FxException;
import io.iconator.monitor.transaction.TransactionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import javax.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import static com.github.rholder.retry.WaitStrategies.randomWait;

@Component
abstract public class BaseMonitor {

    private final static Logger LOG = LoggerFactory.getLogger(BaseMonitor.class);

    private final static long MINUTE_IN_MS = 60 * 1000;

    private final MonitorService monitorService;
    private final PaymentLogService paymentLogService;
    private final FxService fxService;
    final ICOnatorMessageService messageService;

    @Autowired
    private MonitorAppConfig appConfig;

    public BaseMonitor(MonitorService monitorService,
                       PaymentLogService paymentLogService,
                       FxService fxService,
                       ICOnatorMessageService messageService) {
        this.monitorService = monitorService;
        this.paymentLogService = paymentLogService;
        this.fxService = fxService;
        this.messageService = messageService;
    }

    /**
     * @param receivingAddress the address to which a payment has been made.
     * @return true if this address is monitored. False otherwise.
     */
    abstract protected boolean isAddressMonitored(String receivingAddress);

    protected void processTransaction(TransactionAdapter tx) {
        try {
            if (!isTransactionIdAvailable(tx)) return;
            if (!isReceivingAddressAvailable(tx)) return;
            if (!isAddressMonitored(tx.getReceivingAddress())) return;
            String txId = tx.getTransactionId();
            CurrencyType currency = tx.getCurrencyType();
            PaymentLog paymentLog;
            Date now = new Date();
            if (paymentLogService.exists(txId, currency)) {
                paymentLog = paymentLogService.getPaymentLog(txId, currency).get();
                if (paymentLog.wasFullyProcessed()) return;
                if (paymentLog.wasCreatedRecently(MINUTE_IN_MS)) return;
                LOG.info("Found payment which was not fully processed. Resetting creation date and restarting processing over again.");
                paymentLog.setCreateDate(now);
            } else {
                paymentLog = new PaymentLog(txId, now, currency);
            }
            paymentLogService.saveImmediately(paymentLog);
            EligibleForRefund refundEntry = new EligibleForRefund(txId, currency);
            if (!checkAttributesAndUpdatePaymentLog(tx, paymentLog, refundEntry)) return;
            paymentLogService.saveImmediately(paymentLog);

            LOG.debug("Calling token allocation with {} USD for {} transaction {}.", paymentLog.getUsdValue().toPlainString(), currency, txId);
            TokenAllocationResult result;
            result = allocateTokensWithRetries(paymentLog, refundEntry);
            LOG.debug("Allocated {} tomics for {} transaction {}.", result.getAllocatedTomics(), tx.getCurrencyType().name(), tx.getTransactionId());
            BigInteger tomics = result.getAllocatedTomics();
            LOG.debug("For {} USD {} atomic tokens where allocated. {} transaction {}.", paymentLog.getUsdValue().toPlainString(), tomics, tx.getCurrencyType().name(), tx.getTransactionId());
            createAndSendConfirmationMail(tx, tomics);
            LOG.info("Transaction processed: {} {} / {} USD / {} FX / investor id {} / Time: {} / Tomics Amount {}",
                    tx.getTransactionValueInMainUnit(), tx.getCurrencyType().name(), paymentLog.getUsdValue(),
                    paymentLog.getUsdFxRate(), tx.getAssociatedInvestorId(), paymentLog.getCreateDate(),
                    paymentLog.getTomicsAmount());

        } catch (Throwable t) {
            LOG.error("Failed to process {} transaction {}.", tx.getCurrencyType().name(), tx.getTransactionId(), t);
        }
    }

    /*
       * TODO [claude] finish documentation
     * This is method is not holding any of the conversoin or distribution functionality but sits in
     * this class because the method that it calls needs to be transactional and the transaction
     * behavior of spring does only work if one object calls the transactional methods of another
     * object. If this method where in the same class as the actual conversion and distribution
     * method calling that method would not lead to a transactional execution of the code.
     */
    private TokenAllocationResult allocateTokensWithRetries(PaymentLog paymentLog, EligibleForRefund refundEntry)
            throws Throwable {

        String txId = refundEntry.getTxIdentifier();
        if (!txId.contentEquals(paymentLog.getTxIdentifier())) {
            throw new IllegalArgumentException("Transaction Id of refund entry and of payment log must not be different.");
        }
        if (paymentLog.getUsdValue() == null) {
            throw new IllegalArgumentException("PaymentLog's amount in USD must not be null.");
        }
        if (paymentLog.getBlockDate() == null) {
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
                    () -> monitorService.allocateTokens(paymentLog, refundEntry));
            if (result == null) throw new NoSuchElementException();
            return result;
        } catch (Throwable e) {
            LOG.error("Failed to distribute payment to tiers for {} transaction {}.", paymentLog.getCurrency().name(), paymentLog.getTxIdentifier(), e.getCause());
            refundEntry.setRefundReason(RefundReason.CONVERSION_TO_TOKENS_FAILED);
            monitorService.saveNewRefundEntryAndAddItToPaymentLog(refundEntry, paymentLog);
            throw e;
        }
    }

    /**
     * Checks if all necessary attributes on the transaction are available and
     * updates the given payment log and refund entry with the available attributes.
     * If an attribute can not be fetched the given refund entry is written to the
     * database with a corresponding refund reason. The payment log is also saved with
     * a reference to the refund entry.
     *
     * @param tx          the Transaction to be checked
     * @param paymentLog  the payment log to be updated
     * @param refundEntry the refund entry to be updated and used in case of
     *                    missing attributes. It must not already exist on the
     *                    database.
     * @return true if all attributes are available. Flase otherwise.
     */
    private boolean checkAttributesAndUpdatePaymentLog(TransactionAdapter tx, PaymentLog paymentLog, EligibleForRefund refundEntry) {
        if (!isPaymentAmountAvailable(tx)) {
            refundEntry.setRefundReason(RefundReason.TRANSACTION_VALUE_MISSING);
            monitorService.saveNewRefundEntryAndAddItToPaymentLog(refundEntry, paymentLog);
            return false;
        } else {
            paymentLog.setPaymentAmount(tx.getTransactionValue());
            refundEntry.setAmount(tx.getTransactionValue());
        }

        if (!isInvestorAvailable(tx)) {
            refundEntry.setRefundReason(RefundReason.INVESTOR_MISSING);
            monitorService.saveNewRefundEntryAndAddItToPaymentLog(refundEntry, paymentLog);
            return false;
        } else {
            paymentLog.setInvestorId(tx.getAssociatedInvestorId());
            refundEntry.setInvestorId(tx.getAssociatedInvestorId());
        }

        if (!isBlockTimeAvailable(tx)) {
            refundEntry.setRefundReason(RefundReason.BLOCK_TIME_MISSING);
            monitorService.saveNewRefundEntryAndAddItToPaymentLog(refundEntry, paymentLog);
            return false;
        } else {
            paymentLog.setBlockDate(tx.getBlockTime());
        }

        if (!isBlockHeightAvailable(tx)) {
            refundEntry.setRefundReason(RefundReason.BLOCK_HEIGHT_MISSING);
            monitorService.saveNewRefundEntryAndAddItToPaymentLog(refundEntry, paymentLog);
            return false;
        }

        if (!isUSDExchangeRateAvailable(tx)) {
            refundEntry.setRefundReason(RefundReason.FX_RATE_MISSING);
            monitorService.saveNewRefundEntryAndAddItToPaymentLog(refundEntry, paymentLog);
            return false;
        } else {
            try {
                paymentLog.setUsdFxRate(
                        fxService.getUSDExchangeRate(tx.getBlockHeight(), tx.getCurrencyType()));
                paymentLog.setUsdValue(
                        tx.getTransactionValueInMainUnit().multiply(paymentLog.getUsdFxRate()));
            } catch (FxException e) {
                throw new IllegalStateException("Failed fetching Usd to " + tx.getCurrencyType().name() + " exchange rate for transaction " + tx.getTransactionId() + " even after availability check was successful.");
            }
        }
        return true;
    }

    private boolean isTransactionIdAvailable(TransactionAdapter tx) {
        try {
            String txId = tx.getTransactionId();
            if (txId == null || txId.isEmpty()) throw new NoSuchElementException();
        } catch (Throwable t) {
            LOG.error("Failed fetching {} transaction identifier.", tx.getCurrencyType().name(), t);
            return false;
        }
        return true;
    }

    /**
     * Checks if the receiving address can be fetched from the given transaction.
     * If no receiving address can be fetched the given transaction can not be
     * processed since it cannot be checked if it is monitored.
     *
     * @param tx the transaction for which the receiving address should be verified.
     */
    private boolean isReceivingAddressAvailable(TransactionAdapter tx) {
        try {
            String address = tx.getReceivingAddress();
            if (address == null || address.isEmpty()) throw new NoSuchElementException();
        } catch (Throwable t) {
            LOG.error("Failed fetching {} receiving address for transaction {}.", tx.getCurrencyType().name(), tx.getTransactionId(), t);
            return false;
        }
        return true;
    }

    private boolean isPaymentAmountAvailable(TransactionAdapter tx) {
        try {
            BigInteger value = tx.getTransactionValue();
            if (value == null) throw new NoSuchElementException();
        } catch (Throwable t) {
            LOG.error("Failed fetching payment amount for {} transaction {}.", tx.getCurrencyType().name(), tx.getTransactionId(), t);
            return false;
        }
        return true;
    }

    private boolean isInvestorAvailable(TransactionAdapter tx) {
        try {
            Long id = tx.getAssociatedInvestorId();
            if (id == null || id == 0) throw new NoSuchElementException();
        } catch (Throwable t) {
            LOG.error("Couldn't find investor with {} payment address {} for transaction {}.", tx.getCurrencyType().name(), tx.getReceivingAddress(), tx.getTransactionId());
            return false;
        }
        return true;
    }

    private boolean isBlockTimeAvailable(TransactionAdapter tx) {
        try {
            Date blockTime = tx.getBlockTime();
            if (blockTime == null) throw new NoSuchElementException();
        } catch (Throwable t) {
            LOG.error("Failed fetching block time for {} transaction {}.",
                    tx.getCurrencyType().name(), tx.getTransactionId(), t);
            return false;
        }
        return true;
    }

    private boolean isBlockHeightAvailable(TransactionAdapter tx) {
        try {
            Long blockHeight = tx.getBlockHeight();
            if (blockHeight == null) throw new NoSuchElementException();
        } catch (Throwable t) {
            LOG.error("Failed fetching block height for {} transaction {}.",
                    tx.getCurrencyType().name(), tx.getTransactionId(), t);
            return false;
        }
        return true;
    }

    private boolean isUSDExchangeRateAvailable(TransactionAdapter tx) {
        try {
            BigDecimal USDExchangeRate = fxService.getUSDExchangeRate(tx.getBlockHeight(), tx.getCurrencyType());
            if (USDExchangeRate == null) throw new NoSuchElementException();
        } catch (FxException | RuntimeException e) {
            LOG.error("Couldn't get USD to {} exchange rate for transaction {}.", tx.getCurrencyType().name(), tx.getTransactionId(), e);
            return false;
        }
        return true;
    }

    private void createAndSendConfirmationMail(TransactionAdapter tx, BigInteger tomics) {
        Investor investor = investorService.getInvestorByInvestorId(tx.getAssociatedInvestorId());

        messageService.send(new FundsReceivedEmailMessage(
                MessageDTOHelper.build(investor),
                tx.getTransactionValueInMainUnit(),
                tx.getCurrencyType(),
                tx.getWebLinkToTransaction(),
                monitorService.convertTomicsToTokens(tomics)));
    }
}
