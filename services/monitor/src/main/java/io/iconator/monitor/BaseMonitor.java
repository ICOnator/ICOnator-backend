package io.iconator.monitor;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import io.iconator.commons.amqp.model.FundsReceivedEmailMessage;
import io.iconator.commons.amqp.model.utils.MessageDTOHelper;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.db.services.InvestorService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.db.services.exception.InvestorNotFoundException;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.EligibleForRefund.RefundReason;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.monitor.config.MonitorAppConfig;
import io.iconator.monitor.exception.IncompleteTransactoinInformationException;
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
            PaymentLog paymentLog = monitorService.getOrCreatePaymentLog(txId, currency);
            if (paymentLog == null) return;
            paymentLog = checkAttributesAndUpdatePaymentLog(tx, paymentLog);
            if (paymentLog == null) return;
            LOG.debug("Calling token allocation with {} USD for {} transaction {}.", paymentLog.getUsdValue().toPlainString(), currency, txId);
            TokenAllocationResult result;
            result = allocateTokensWithRetries(paymentLog);
            LOG.debug("Allocated {} tomics for {} transaction {}.", result.getAllocatedTomics(), tx.getCurrencyType().name(), tx.getTransactionId());
            BigInteger tomics = result.getAllocatedTomics();
            LOG.debug("For {} USD {} atomic tokens where allocated. {} transaction {}.", paymentLog.getUsdValue().toPlainString(), tomics, tx.getCurrencyType().name(), tx.getTransactionId());
            createAndSendTokenAllocationMail(tx, tomics);
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
    public TokenAllocationResult allocateTokensWithRetries(PaymentLog paymentLog)
            throws Throwable {

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
                    () -> monitorService.allocateTokens(paymentLog));
            if (result == null) throw new NoSuchElementException();
            return result;
        } catch (Throwable e) {
            LOG.error("Failed to distribute payment to tiers for {} transaction {}.", paymentLog.getCurrency().name(), paymentLog.getTxIdentifier(), e.getCause());
            RefundReason reason = RefundReason.CONVERSION_TO_TOKENS_FAILED;
            monitorService.createRefundEntryForPaymentLogAndCommit(paymentLog, reason);
            throw e;
        }
    }

    /**
     * Checks if all necessary attributes on the transaction are available and
     * updates and saves the given payment log with the available attributes.
     * If an attribute can not be fetched a refund entry is written to the
     * database with a corresponding refund reason and the payment references
     * the refund entry.
     *
     * @param tx         the Transaction to be checked
     * @param paymentLog the payment log to be updated
     * @return the updated PaymentLog or null if the transaction attributes
     * could not all be fetched.
     */
    // TODO [claude]: Should throw exception instead of returning null.
    private PaymentLog checkAttributesAndUpdatePaymentLog(TransactionAdapter tx, PaymentLog paymentLog) {
        RefundReason reason = null;
        try {
            if (!isPaymentAmountAvailable(tx)) {
                reason = RefundReason.TRANSACTION_VALUE_MISSING;
                throw new IncompleteTransactoinInformationException();
            } else {
                paymentLog.setPaymentAmount(tx.getTransactionValue());
            }

            if (!isInvestorAvailable(tx)) {
                reason = RefundReason.INVESTOR_MISSING;
                throw new IncompleteTransactoinInformationException();
            } else {
                try {
                    paymentLog.setInvestorId(tx.getAssociatedInvestorId());
                } catch (InvestorNotFoundException ignore) {}
            }

            if (!isBlockTimeAvailable(tx)) {
                reason = RefundReason.BLOCK_TIME_MISSING;
                throw new IncompleteTransactoinInformationException();
            } else {
                paymentLog.setBlockDate(tx.getBlockTime());
            }

            if (!isBlockHeightAvailable(tx)) {
                reason = RefundReason.BLOCK_HEIGHT_MISSING;
                throw new IncompleteTransactoinInformationException();
            }

            if (!isUSDExchangeRateAvailable(tx)) {
                reason = RefundReason.FX_RATE_MISSING;
                throw new IncompleteTransactoinInformationException();
            } else {
                try {
                    paymentLog.setUsdFxRate(
                            fxService.getUSDExchangeRate(tx.getBlockHeight(), tx.getCurrencyType()));
                    paymentLog.setUsdValue(
                            tx.getTransactionValueInMainUnit().multiply(paymentLog.getUsdFxRate()));
                } catch (FxException ignore) {}
            }
        } catch (IncompleteTransactoinInformationException e) {
            monitorService.createRefundEntryForPaymentLogAndCommit(paymentLog, reason);
            return null;
        }
        return paymentLogService.saveAndCommit(paymentLog);
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
            if (id == null) throw new NoSuchElementException();
        } catch (Throwable t) {
            LOG.error("Couldn't find investor with {} payment address {} for transaction {}.", tx.getCurrencyType().name(), tx.getReceivingAddress(), tx.getTransactionId(), t);
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

    private void createAndSendTokenAllocationMail(TransactionAdapter tx, BigInteger tomics) {
        Investor investor;
        try {
            investor = investorService.getInvestorByInvestorId(tx.getAssociatedInvestorId());
        } catch (InvestorNotFoundException e) {
            throw new IllegalStateException("Failed fetching investor for transaction " + tx.getTransactionId() + " even after availability check was successful.");
        }

        messageService.send(new FundsReceivedEmailMessage(
                MessageDTOHelper.build(investor),
                tx.getTransactionValueInMainUnit(),
                tx.getCurrencyType(),
                tx.getWebLinkToTransaction(),
                monitorService.convertTomicsToTokens(tomics)));
    }
}
