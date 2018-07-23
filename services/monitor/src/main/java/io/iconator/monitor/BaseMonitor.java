package io.iconator.monitor;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import io.iconator.commons.db.services.EligibleForRefundService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.EligibleForRefund;
import io.iconator.commons.model.db.EligibleForRefund.RefundReason;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.monitor.config.MonitorAppConfig;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.TokenConversionService;
import io.iconator.monitor.service.TokenConversionService.TokenDistributionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import javax.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.github.rholder.retry.WaitStrategies.randomWait;

@Component
public class BaseMonitor {

    @Autowired
    private MonitorAppConfig appConfig;

    private final static Logger LOG = LoggerFactory.getLogger(BaseMonitor.class);

    protected final TokenConversionService tokenConversionService;
    protected final InvestorRepository investorRepository;
    protected final EligibleForRefundService eligibleForRefundService;
    protected final PaymentLogService paymentLogService;
    protected final FxService fxService;

    public BaseMonitor(TokenConversionService tokenConversionService,
                       InvestorRepository investorRepository,
                       PaymentLogService paymentLogService,
                       EligibleForRefundService eligibleForRefundService,
                       FxService fxService) {
        this.tokenConversionService = tokenConversionService;
        this.investorRepository = investorRepository;
        this.paymentLogService = paymentLogService;
        this.eligibleForRefundService = eligibleForRefundService;
        this.fxService = fxService;
    }

    protected boolean isTransactionUnprocessed(String txIdentifier) {
        return !paymentLogService.existsByTxIdentifier(txIdentifier)
                && !eligibleForRefundService.existsByTxIdentifier(txIdentifier);
    }

    /**
     *
     * @param amount
     * @param currencyType
     * @param txIdentifier
     * @param reason
     * @param investor
     */
    protected void eligibleForRefund(BigInteger amount,
                                     CurrencyType currencyType,
                                     String txIdentifier,
                                     RefundReason reason,
                                     Investor investor) {

        long investorId = investor != null ? investor.getId() : 0;
        EligibleForRefund eligibleForRefund = new EligibleForRefund(reason, amount, currencyType,
                investorId, txIdentifier);
        try {
            LOG.info("Creating refund entry for transaction {}.", txIdentifier);
            // Saving without a transaction will persist to the database immediatly which makes sure
            // that other monitor apps that run concurrently will see that this transaction has
            // already been processed by a monitor app.
            eligibleForRefundService.saveTransactionless(eligibleForRefund);
        } catch (Exception e) {
            if (eligibleForRefundService.existsByTxIdentifier(txIdentifier)) {
                LOG.info("Couldn't create refund entry because it already existed. " +
                        "I.e. transaction was already processed.", e);
            } else {
                LOG.error("Failed creating refund entry.", e);
            }
        }
    }

    /**
     * // TODO [claude, 2018-07-19], Finish documentation
     * This is method is not holding any of the conversoin or distribution functionality but sits in
     * this class because the method that it calls needs to be transactional and the transaction
     * behavior of spring does only work if one object calls the transactional methods of another
     * object. If this method where in the same class as the actual conversion and distribution
     * method calling that method would not lead to a transactional execution of the code.
     * @param usd
     * @param blockTime
     * @return
     * @throws Throwable
     */
    public TokenDistributionResult convertAndDistributeToTiersWithRetries(BigDecimal usd, Date blockTime)
            throws Throwable {

        if (blockTime == null) throw new IllegalArgumentException("Block time must not be null.");
        if (usd == null) throw new IllegalArgumentException("USD amount must not be null.");

        // Retry as long as there are database locking exceptions.
        Retryer<TokenDistributionResult> retryer = RetryerBuilder.<TokenDistributionResult>newBuilder()
                .retryIfExceptionOfType(OptimisticLockingFailureException.class)
                .retryIfExceptionOfType(OptimisticLockException.class)
                .withWaitStrategy(randomWait(appConfig.getTokenConversionMaxTimeWait(), TimeUnit.MILLISECONDS))
                .withStopStrategy(StopStrategies.neverStop())
                .build();

        try {
            return retryer.call(() -> tokenConversionService.convertAndDistributeToTiers(usd, blockTime));
        } catch (ExecutionException | RetryException e) {
            LOG.error("Currency to token conversion failed.", e);
            throw e.getCause();
        }
    }
}
