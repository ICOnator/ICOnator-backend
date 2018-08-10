package io.iconator.monitor;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.google.common.base.VerifyException;
import io.iconator.commons.db.services.EligibleForRefundService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.model.db.EligibleForRefund;
import io.iconator.commons.model.db.EligibleForRefund.RefundReason;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.monitor.config.MonitorAppConfig;
import io.iconator.monitor.exception.TransactionAlreadyProcessedException;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.TokenAllocationService;
import io.iconator.monitor.service.TokenAllocationService.TokenAllocationResult;
import io.iconator.monitor.service.exceptions.FxException;
import io.iconator.monitor.transaction.Transaction;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.github.rholder.retry.WaitStrategies.randomWait;

@Component
public class BaseMonitor {

    @Autowired
    private MonitorAppConfig appConfig;

    private final static Logger LOG = LoggerFactory.getLogger(BaseMonitor.class);

    protected final TokenAllocationService tokenAllocationService;
    protected final InvestorRepository investorRepository;
    protected final EligibleForRefundService eligibleForRefundService;
    protected final PaymentLogService paymentLogService;
    protected final FxService fxService;

    public BaseMonitor(TokenAllocationService tokenAllocationService,
                       InvestorRepository investorRepository,
                       PaymentLogService paymentLogService,
                       EligibleForRefundService eligibleForRefundService,
                       FxService fxService) {
        this.tokenAllocationService = tokenAllocationService;
        this.investorRepository = investorRepository;
        this.paymentLogService = paymentLogService;
        this.eligibleForRefundService = eligibleForRefundService;
        this.fxService = fxService;
    }

    /**
     * // TODO [claude, 2018-07-19], Finish documentation
     * This is method is not holding any of the conversoin or distribution functionality but sits in
     * this class because the method that it calls needs to be transactional and the transaction
     * behavior of spring does only work if one object calls the transactional methods of another
     * object. If this method where in the same class as the actual conversion and distribution
     * method calling that method would not lead to a transactional execution of the code.
     *
     * @param usd
     * @param blockTime
     * @return
     * @throws Throwable
     */
    public TokenAllocationService.TokenAllocationResult allocateTokensWithRetries(BigDecimal usd, Date blockTime)
            throws Throwable {

        if (blockTime == null) throw new IllegalArgumentException("Block time must not be null.");
        if (usd == null) throw new IllegalArgumentException("USD amount must not be null.");

        // Retry as long as there are database locking exceptions.
        Retryer<TokenAllocationService.TokenAllocationResult> retryer =
                RetryerBuilder.<TokenAllocationService.TokenAllocationResult>newBuilder()
                        .retryIfExceptionOfType(OptimisticLockingFailureException.class)
                        .retryIfExceptionOfType(OptimisticLockException.class)
                        .withWaitStrategy(randomWait(appConfig.getTokenConversionMaxTimeWait(), TimeUnit.MILLISECONDS))
                        .withStopStrategy(StopStrategies.neverStop())
                        .build();

        try {
            return retryer.call(() -> tokenAllocationService.allocateTokens(usd, blockTime));
        } catch (ExecutionException | RetryException e) {
            LOG.error("Currency to token conversion failed.", e);
            throw e.getCause();
        }
    }

    /**
     * Checks if the transaction of the given payment log has not yet been
     * processed and saves the payment log transactionless, which means that it
     * is immediatly commited to the database.
     *
     * @param paymentLog The PaymentLog entity to be stored.
     * @throws TransactionAlreadyProcessedException if a PaymentLog entry or an EligibleForRefund
     *                                              entry exists for the transaction of the given
     *                                              PaymentLog entity.
     */
    protected PaymentLog savePaymentLog(PaymentLog paymentLog) {
        if (txIsAlreadyProcessed(paymentLog.getTxIdentifier())) {
            LOG.info("Couldn't create payment log entry because transaction " +
                    "was already processed.", paymentLog.getTxIdentifier());
            return null;
        }
        LOG.info("Creating payment log entry for transaction {}.", paymentLog.getTxIdentifier());
        return paymentLogService.saveTransactionless(paymentLog);
    }

    /**
     * Checks if the transaction referenced by the given EligibleForRefund has
     * not yet been processed. If not, saves the given entry transactionless,
     * which means that it is immediatly commited to the database.
     *
     * @throws TransactionAlreadyProcessedException if a PaymentLog entry or an EligibleForRefund
     *                                              entry exists for the transaction of the given
     *                                              PaymentLog entity.
     */
    protected EligibleForRefund saveRefundEntry(EligibleForRefund refundEntry) {
        if (txIsAlreadyProcessed(refundEntry.getTxIdentifier())) {
            LOG.info("Couldn't create refund entry because transaction " +
                    "was already processed.", refundEntry.getTxIdentifier());
            return null;
        }
        LOG.info("Creating refund entry for transaction {}.", refundEntry.getTxIdentifier());
        return eligibleForRefundService.saveTransactionless(refundEntry);
    }

    protected boolean txIsAlreadyProcessed(String txId) {
        return eligibleForRefundService.existsByTxIdentifier(txId) ||
                paymentLogService.existsByTxIdentifier(txId);
    }

    protected void processTransaction(Transaction tx) {
        try {
            verifyTransaction(tx);
        } catch (VerifyException e) {

        }

        EligibleForRefund.Builder refundBuilder = new EligibleForRefund.Builder();
        Investor i = investorRepository.findOptionalByPayInEtherAddressIgnoreCase(tx.getReceivingAddress()).get();
        refundBuilder.currency(tx.getCurrencyType())
                .txIdentifier(tx.getTransactionId())
                .amount(tx.getTransactionValue())
                .investorId(i.getId());

        BigDecimal USDperCurrency, usdReceived;
        try {
            USDperCurrency = fxService.getUSDperCryptoCurrency(tx.getBlockHeight(), tx.getCurrencyType());
            LOG.debug("Got USD to {} rate {} from FxService, transaction id {}, receiving address {}",
                    tx.getCurrencyType().toString(), USDperCurrency.toPlainString(),
                    tx.getTransactionId(), tx.getReceivingAddress());
            usdReceived = tx.getTransactionValueInMainUnit().multiply(USDperCurrency);
        } catch (FxException e) {
            LOG.error("Couldn't get USD to {} exchange rate for transaction {}.",
                    tx.getCurrencyType().toString(), tx.getTransactionId(), e);
            saveRefundEntry(refundBuilder.refundReason(RefundReason.FX_RATE_MISSING).build());
            return;
        } catch (RuntimeException e) {
            LOG.error("Failed to fetch payment amount in US dollars for {} transaction {}.",
                    tx.getCurrencyType().toString(), tx.getTransactionId(), e);
            saveRefundEntry(refundBuilder.refundReason(RefundReason.CONVERSION_TO_USD_FAILED).build());
            return;
        }

        LOG.debug("USD {} to be converted to tokens, for {} transaction {}",
                usdReceived.toPlainString(), tx.getCurrencyType().toString(), tx.getTransactionId());
        PaymentLog paymentLog = createAndSavePaymentLog(tx, refundBuilder, i, USDperCurrency, usdReceived);
        if (paymentLog == null) return;

        // TODO: if monitor stops at this point (e.g. due to an outage) the
        // transaction will have a PaymentLog but was not fully processed.

        TokenAllocationResult result;
        try {
            LOG.debug("Calling token allocation with {} USD for {} transaction {}.",
                    usdReceived, tx.getCurrencyType().toString(), tx.getTransactionId());
            result = allocateTokensWithRetries(usdReceived, tx.getBlockTime());
        } catch (Throwable e) {
            LOG.error("Failed to distribute payment to tiers for {} transaction {}. " +
                            "Deleting PaymentLog this transaction", tx.getCurrencyType().toString(),
                    tx.getTransactionId(), e);
            paymentLogService.delete(paymentLog);
            saveRefundEntry(refundBuilder.refundReason(RefundReason.CONVERSION_TO_TOKENS_FAILED).build());
            return;
        }
        BigInteger tomics = result.getDistributedTomics();
        LOG.debug("For {} USD {} atomic tokens where allocated. {} transaction {}.",
                usdReceived, tomics, tx.getCurrencyType().toString(), tx.getTransactionId());

        paymentLog.setTomicsAmount(tomics);
        paymentLog = paymentLogService.save(paymentLog);

        if (result.hasOverflow()) {
            BigInteger overflowWei = BitcoinUtils.convertUsdToSatoshi(result.getOverflow(), USDperETH);
            LOG.debug("The payment of {} generated an overflow of {} {}, which go into the refund table.",
                    tx.getTransactionValueInMainUnit(), tx.getCurrencyType().toString(), overflowWei);
            saveRefundEntry(overflowWei, CurrencyType.ETH, txIdentifier, RefundReason.TOKEN_OVERFLOW, investor);
        }
//
//        final String etherscanLink = "https://etherscan.io/tx/" + txIdentifier;
//
//        messageService.send(new FundsReceivedEmailMessage(
//                build(investor),
//                ethers,
//                CurrencyType.ETH,
//                etherscanLink,
//                tokenAllocationService.convertTomicsToTokens(tomics)));
//
//        LOG.info("Pay-in received: {} ETH / {} USD / {} FX / {} / Time: {} / Address: {} / " +
//                        "Tomics Amount {}",
//                ethers,
//                paymentLog.getPaymentAmount(),
//                paymentLog.getFxRate(),
//                investor.getEmail(),
//                paymentLog.getCreateDate(),
//                receivingAddress,
//                paymentLog.getTomicsAmount());
    }

    private PaymentLog createAndSavePaymentLog(Transaction tx,
                                               EligibleForRefund.Builder refundBuilder,
                                               Investor i,
                                               BigDecimal USDperCurrency,
                                               BigDecimal usdReceived) {

        PaymentLog paymentLog = new PaymentLog(
                tx.getTransactionId(),
                new Date(),
                tx.getBlockTime(),
                tx.getCurrencyType(),
                tx.getTransactionValue(),
                USDperCurrency,
                usdReceived,
                i.getId(),
                BigInteger.ZERO);
        try {
            return savePaymentLog(paymentLog);
        } catch (Throwable t) {
            LOG.error("Failed creating payment log for transaction {}.", tx.getTransactionId(), t);
            saveRefundEntry(refundBuilder.refundReason(RefundReason.CREATING_PAYMENTLOG_FAILED).build());
            return null;
        }
    }

    private void verifyTransaction(Transaction tx)
            throws VerifyException {

        EligibleForRefund.Builder refundBuilder = new EligibleForRefund.Builder();
        refundBuilder.currency(tx.getCurrencyType());

        verifyTransactionId(tx);
        refundBuilder.txIdentifier(tx.getTransactionId());

        verifyValue(tx, refundBuilder);
        refundBuilder.amount(tx.getTransactionValue());

        verifyReceivingAddress(tx, refundBuilder);
        verifyInvestor(tx, refundBuilder);
        Investor i = investorRepository.findOptionalByPayInEtherAddressIgnoreCase(tx.getReceivingAddress()).get();
        refundBuilder.investorId(i.getId());

        verifyBlockTime(tx, refundBuilder);
        verifyBlockHeight(tx, refundBuilder);
    }

    private void verifyTransactionId(Transaction tx) {
        try {
            String txId = tx.getTransactionId();
            if (txId == null || txId.isEmpty()) throw new NoSuchElementException();
        } catch (Throwable t) {
            LOG.error("Failed fetching {} transaction identifier.", tx.getCurrencyType().toString(), t);
            throw new VerifyException();
        }
    }

    private void verifyValue(Transaction tx, EligibleForRefund.Builder builder) {
        try {
            BigInteger value = tx.getTransactionValue();
            if (value == null) throw new NoSuchElementException();
        } catch (Throwable t) {
            LOG.error("Failed fetching value for {} transaction {}.",
                    tx.getCurrencyType().toString(), tx.getTransactionId(), t);
            saveRefundEntry(builder.refundReason(RefundReason.TRANSACTION_VALUE_MISSING).build());
            throw new VerifyException();
        }
    }

    private void verifyReceivingAddress(Transaction tx, EligibleForRefund.Builder builder) {
        try {
            String address = tx.getReceivingAddress();
            if (address == null || address.isEmpty()) throw new NoSuchElementException();
        } catch (Throwable t) {
            LOG.error("Failed fetching {} receiving address for transaction {}.",
                    tx.getCurrencyType().toString(), tx.getTransactionId(), t);
            saveRefundEntry(builder.refundReason(RefundReason.RECEIVING_ADDRESS_MISSING).build());
            throw new VerifyException();
        }
    }

    private void verifyInvestor(Transaction tx, EligibleForRefund.Builder builder) {
        String address = tx.getReceivingAddress();
        try {
            investorRepository.findOptionalByPayInEtherAddressIgnoreCase(address).get();
        } catch (Throwable t) {
            LOG.error("Couldn't find investor with public address {} for {} transaction {}.",
                    address, tx.getCurrencyType().toString(), tx.getTransactionId());
            saveRefundEntry(builder.refundReason(RefundReason.INVESTOR_MISSING).build());
            throw new VerifyException();
        }
    }

    private void verifyBlockTime(Transaction tx, EligibleForRefund.Builder builder) {
        try {
            Date blockTime = tx.getBlockTime();
            if (blockTime == null) throw new NoSuchElementException();
        } catch (Throwable t) {
            LOG.error("Failed fetching block time for {} transaction {}.",
                    tx.getCurrencyType().toString(), tx.getTransactionId(), t);
            saveRefundEntry(builder.refundReason(RefundReason.BLOCK_TIME_MISSING).build());
            throw new VerifyException();
        }
    }

    private void verifyBlockHeight(Transaction tx, EligibleForRefund.Builder builder) {
        try {
            Long blockHeight = tx.getBlockHeight();
            if (blockHeight == null) throw new NoSuchElementException();
        } catch (Throwable t) {
            LOG.error("Failed fetching block height for {} transaction {}.",
                    tx.getCurrencyType().toString(), tx.getTransactionId(), t);
            saveRefundEntry(builder.refundReason(RefundReason.BLOCK_HEIGHT_MISSING).build());
            throw new VerifyException();
        }
    }
}
