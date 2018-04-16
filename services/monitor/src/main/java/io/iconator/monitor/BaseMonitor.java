package io.iconator.monitor;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.Unit;
import io.iconator.commons.model.db.EligibleForRefund;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.EligibleForRefundRepository;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.commons.sql.dao.PaymentLogRepository;
import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.monitor.config.MonitorAppConfig;
import io.iconator.monitor.service.FxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.github.rholder.retry.WaitStrategies.randomWait;

@Component
public class BaseMonitor {

    private final static Logger LOG = LoggerFactory.getLogger(BitcoinMonitor.class);

    protected static final MathContext MATH_CONTEXT = new MathContext(34, RoundingMode.DOWN);

    private final SaleTierRepository saleTierRepository;
    protected final InvestorRepository investorRepository;
    protected final PaymentLogRepository paymentLogRepository;
    protected final EligibleForRefundRepository eligibleForRefundRepository;
    protected final FxService fxService;

    @Autowired
    private MonitorAppConfig appConfig;

    public BaseMonitor(SaleTierRepository saleTierRepository, InvestorRepository investorRepository,
                       PaymentLogRepository paymentLogRepository,
                       EligibleForRefundRepository eligibleForRefundRepository, FxService fxService) {
        this.saleTierRepository = saleTierRepository;
        this.investorRepository = investorRepository;
        this.paymentLogRepository = paymentLogRepository;
        this.eligibleForRefundRepository = eligibleForRefundRepository;
        this.fxService = fxService;
    }


    /**
     * The returned pair consists of
     * - key:   the tokens that the given amount is worth
     * - value: amount which could not be converted into tokens because all tiers where already full.
     * <p>
     * Converts with the assumption of an exchange rate of 1:1 of the given input to the tokens
     * (smallest unit of the token).
     * <p>
     * Converted amount is rounded down to the next whole number if it is not an integer.
     */
    public ConversionResult convertToTokensAndUpdateTiers(BigDecimal amount, Date blockTimestamp)
            throws Throwable {

        if (blockTimestamp == null) {
            throw new IllegalArgumentException("Block time stamp must not be null.");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Amount must must not be null.");
        }

        Retryer<ConversionResult> retryer = RetryerBuilder.<ConversionResult>newBuilder()
                    .retryIfExceptionOfType(OptimisticLockException.class)
                    .withWaitStrategy(randomWait(
                        appConfig.getTokenConversionMaxTimeWait(),
                        TimeUnit.MILLISECONDS))
                    .withStopStrategy(StopStrategies.neverStop())
                    .build();

        Callable<ConversionResult> callable =
                () -> convertToTokensAndUpdateTiersInternal(amount, blockTimestamp);

        try {
            return retryer.call(callable);
        } catch (ExecutionException e) {
            throw e.getCause();
        } catch (RetryException e) {
            // should never happen because retryer is set to never stop tyring.
        }
        return null;
    }

    @Transactional(rollbackFor = OptimisticLockException.class)
    protected ConversionResult convertToTokensAndUpdateTiersInternal(BigDecimal amount, Date blockTime) {
        BigDecimal remainingAmount = amount;
        BigInteger tokensTotal = BigInteger.ZERO;
        Optional<SaleTier> oCurrentTier = saleTierRepository.findByIsActiveTrue();

        while (oCurrentTier.isPresent() && remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            SaleTier currentTier = oCurrentTier.get();

            BigDecimal tokensDecimal = convertCurrencyToTokens(remainingAmount, currentTier.getDiscount());
            BigInteger tokens = tokensDecimal.toBigInteger();

            if (tokensExceedHardcap(tokens, currentTier)) {
                // Tokens must be distributed over multiple tiers
                // Calculate the amount that is assigned to the current tier and what remains for
                // the next tier.
                BigInteger tokensToCurrentTier = currentTier.getTokenMax().subtract(currentTier.getTokensSold());
                currentTier.setTokensSold(currentTier.getTokensSold().add(tokensToCurrentTier));
                tokensTotal = tokensTotal.add(tokensToCurrentTier);
                remainingAmount = convertTokensToCurrency(
                        tokensDecimal.subtract(new BigDecimal(tokensToCurrentTier)),
                        currentTier.getDiscount());

                // Close the filled up tier and open the next one.
                currentTier.setEndDate(blockTime);
                currentTier.setInactive();
                oCurrentTier = getNextTier(currentTier);
                if (oCurrentTier.isPresent()) {
                    oCurrentTier.get().setStartDate(blockTime);
                    oCurrentTier.get().setActive();
                }

            } else {
                // All tokens can be retrieved from the same tier.
                currentTier.setTokensSold(currentTier.getTokensSold().add(tokens));
                tokensTotal = tokensTotal.add(tokens);
                remainingAmount = BigDecimal.ZERO;
            }
        }
        return new ConversionResult(tokensTotal, remainingAmount);
    }

    /**
     * Assumes that the SaleTier's numbers (tierNo) are consecutive by increments of 1.
     */
    protected Optional<SaleTier> getNextTier(SaleTier currentTier) {
        return saleTierRepository.findByTierNo(currentTier.getTierNo() + 1);
    }

    public static class ConversionResult {
        private BigInteger tokens;
        private BigDecimal overflow;

        private ConversionResult(BigInteger tokens, BigDecimal overflow) {
            this.tokens = tokens;
            this.overflow = overflow;
        }

        public boolean hasOverflow() {
            return overflow.compareTo(BigDecimal.ZERO) > 0;
        }

        public BigInteger getTokens() {
            return tokens;
        }

        public BigDecimal getOverflow() {
            return overflow;
        }
    }

    private BigDecimal convertCurrencyToTokens(BigDecimal currency, BigDecimal discountRate) {
        return currency.divide(BigDecimal.ONE.subtract(discountRate), MATH_CONTEXT);
    }

    private BigDecimal convertTokensToCurrency(BigDecimal tokens, BigDecimal discountRate) {
        return tokens.multiply(BigDecimal.ONE.subtract(discountRate));
    }

    private boolean tokensExceedHardcap(BigInteger tokens, SaleTier tier) {
        return tier.getTokensSold().add(tokens).compareTo(tier.getTokenMax()) >= 0;
    }

    protected boolean isTransactionUnprocessed(String txIdentifier) {
        return !paymentLogRepository.existsByTxIdentifier(txIdentifier)
            && !eligibleForRefundRepository.existsByTxIdentifier(txIdentifier);
    }

    protected EligibleForRefund eligibleForRefundInSatoshi(EligibleForRefund.RefundReason reason,
                                                        BigInteger amount,
                                                        String txoIdentifier,
                                                        Investor investor) {

        Unit unit = Unit.SATOSHI;
        CurrencyType currency = CurrencyType.BTC;
        EligibleForRefund entry =
                new EligibleForRefund(reason, amount, unit, currency, investor, txoIdentifier);

        return eligibleForRefundRepository.save(entry);
    }

    protected EligibleForRefund eligibleForRefundInWei(EligibleForRefund.RefundReason reason,
                                                           BigInteger amount,
                                                           String txoIdentifier,
                                                           Investor investor) {

        Unit unit = Unit.WEI;
        CurrencyType currency = CurrencyType.ETH;
        EligibleForRefund entry =
                new EligibleForRefund(reason, amount, unit, currency, investor, txoIdentifier);

        return eligibleForRefundRepository.save(entry);
    }
}
