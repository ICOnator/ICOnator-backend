package io.iconator.monitor.service;

import com.github.rholder.retry.*;
import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.monitor.config.MonitorAppConfig;
import io.iconator.monitor.token.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class TokenConversionService {

    private final static Logger LOG = LoggerFactory.getLogger(TokenConversionService.class);

    @Autowired
    private SaleTierRepository saleTierRepository;

    @Autowired
    private MonitorAppConfig appConfig;

    /**
     * TODO [cmu, 07.06.18]: Update documentation. Mention the retrying behavior.
     * Converts the given amount of currency to tokens and updates the sale tiers accordingly.
     * E.g. if the converted amount of tokens overflows a tier's limit the tier is set inactive and
     * the next tier is activated.
     *
     *
     * @return the result of the conversion. The {@link ConversionResult} consists of
     * <ul>
     * <li>tokens: the tokens that the given amount is worth and was assigned to one or more tiers.
     * <li>overflow: amount which could not be converted into tokens because all tiers where already
     * full.
     * </ul>
     */
    public ConversionResult convertToTokensAndUpdateTiers(BigDecimal usd, Date blockTime)
            throws Throwable {

        if (blockTime == null) {
            throw new IllegalArgumentException("Block time stamp must not be null.");
        }
        if (usd == null) {
            throw new IllegalArgumentException("USD amount must not be null.");
        }

        // Retry as long as there are database locking exceptions.
        Retryer<ConversionResult> retryer = RetryerBuilder.<ConversionResult>newBuilder()
                .retryIfExceptionOfType(OptimisticLockingFailureException.class)
                .retryIfExceptionOfType(OptimisticLockException.class)
                .withWaitStrategy(WaitStrategies.randomWait(
                        appConfig.getTokenConversionMaxTimeWait(),
                        TimeUnit.MILLISECONDS))
                .withStopStrategy(StopStrategies.neverStop())
                .build();

        Callable<ConversionResult> callable =
                () -> convertToTokensAndUpdateTiersInternal(usd, blockTime);

        try {
            return retryer.call(callable);
        } catch (ExecutionException e) {
            LOG.error("Currency to token conversion failed.", e);
            throw e.getCause();
        } catch (RetryException e) {
            // Should never happen because of NeverStopStrategy.
        }
        return null;
    }

    @Transactional(rollbackFor = {Exception.class},
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRES_NEW)
    protected ConversionResult convertToTokensAndUpdateTiersInternal(BigDecimal usd, Date blockTime) {
        BigDecimal usdRemaining = usd;
        BigInteger tokensTotal = BigInteger.ZERO;
        // TODO [cmu, 07.06.18]: Find active tier by date and token amount sold.
        Optional<SaleTier> oCurrentTier = saleTierRepository.findActiveTierByDate(blockTime);

        while (oCurrentTier.isPresent() && usdRemaining.compareTo(BigDecimal.ZERO) > 0) {
            SaleTier currentTier = oCurrentTier.get();

            BigDecimal tokensDecimal = TokenUtils.convertUsdToTokenUnits(usdRemaining, appConfig.getUsdPerToken(),
                    currentTier.getDiscount());
            BigInteger tokensInteger = tokensDecimal.toBigInteger();

            if (tokensFillOrExceedTiersCap(tokensInteger, currentTier)) {
                // Tokens must be distributed over multiple tiers. Calculate the amount that is
                // assigned to the current tier and how much currency is carried over to the next tier.
                BigInteger availableTokensOnTier = currentTier.getTokenMax()
                        .subtract(currentTier.getTokensSold());
                BigDecimal tokenOverflow = tokensDecimal.subtract(new BigDecimal(availableTokensOnTier));
                usdRemaining = TokenUtils.convertTokenUnitsToUsd(tokenOverflow, appConfig.getUsdPerToken(),
                        currentTier.getDiscount());
                tokensTotal = tokensTotal.add(availableTokensOnTier);

                // Update tokens sold on current tier and end its active time.
                currentTier.setTokensSold(currentTier.getTokenMax());
                currentTier.setEndDate(blockTime);
                currentTier = saleTierRepository.save(oCurrentTier.get());

                // Start next tiers active time if present.
                oCurrentTier = saleTierRepository.findByTierNo(currentTier.getTierNo() + 1);
                oCurrentTier.ifPresent(t -> {
                    t.setStartDate(blockTime);
                    saleTierRepository.save(t);
                });
            } else {
                // All tokens can be retrieved from the same tier.
                currentTier.setTokensSold(currentTier.getTokensSold().add(tokensInteger));
                tokensTotal = tokensTotal.add(tokensInteger);
                usdRemaining = BigDecimal.ZERO;
                saleTierRepository.save(oCurrentTier.get());
            }
        }
        saleTierRepository.flush();
        return new ConversionResult(tokensTotal, usdRemaining);
    }

    private boolean tokensFillOrExceedTiersCap(BigInteger tokens, SaleTier tier) {
        return tier.getTokensSold().add(tokens).compareTo(tier.getTokenMax()) >= 0;
    }

    public static class ConversionResult {
        private final BigInteger tokens;
        private final BigDecimal overflow;

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
}
