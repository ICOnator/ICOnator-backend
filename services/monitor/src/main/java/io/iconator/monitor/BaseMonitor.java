package io.iconator.monitor;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import io.iconator.commons.amqp.model.FundsReceivedEmailMessage;
import io.iconator.commons.amqp.model.utils.MessageDTOHelper;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.db.services.InvestorService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.EligibleForRefund.RefundReason;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.monitor.config.MonitorAppConfig;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.MonitorService;
import io.iconator.monitor.service.MonitorService.TokenAllocationResult;
import io.iconator.monitor.transaction.TransactionAdapter;
import io.iconator.monitor.transaction.exception.MissingTransactionInformationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import javax.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.math.BigInteger;
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

    protected void processBuildingTransaction(TransactionAdapter tx) {
        try {
            if (!isAddressMonitored(tx.getReceivingAddress())) return;
            String txId = tx.getTransactionId();
            CurrencyType currency = tx.getCurrencyType();
            PaymentLog paymentLog = monitorService.getOrCreatePaymentLog(txId, currency);
            if (paymentLog == null) return;
            paymentLog = updatePaymentLog(tx, paymentLog);
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
            LOG.error("Error processing transaction.", t);
        }
    }

    private PaymentLog updatePaymentLog(TransactionAdapter tx, PaymentLog paymentLog) {
        RefundReason reason = null;
        try {
            reason = RefundReason.TRANSACTION_VALUE_MISSING;
            paymentLog.setPaymentAmount(tx.getTransactionValue());
            BigDecimal valueInMainUnit = tx.getTransactionValueInMainUnit();

            reason = RefundReason.INVESTOR_MISSING;
            paymentLog.setInvestorId(tx.getAssociatedInvestorId());

            reason = RefundReason.BLOCK_TIME_MISSING;
            paymentLog.setBlockDate(tx.getBlockTime());

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

    private void createAndSendTokenAllocationMail(TransactionAdapter tx, BigInteger tomics) {
        try {
            Investor investor = investorService.getInvestorByInvestorId(tx.getAssociatedInvestorId());

            messageService.send(new FundsReceivedEmailMessage(
                    MessageDTOHelper.build(investor),
                    tx.getTransactionValueInMainUnit(),
                    tx.getCurrencyType(),
                    tx.getWebLinkToTransaction(),
                    monitorService.convertTomicsToTokens(tomics)));
        } catch (Exception ignore) {
            // Missing transaction information or a missing associated investor
            // would have been recognized earlier on in the transaction processing.
        }
    }
}
