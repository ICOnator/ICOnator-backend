package io.iconator.monitor.service;

import com.github.rholder.retry.*;
import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.monitor.config.MonitorAppConfig;
import io.iconator.monitor.service.exceptions.TokenCapOverflowException;
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
        Optional<SaleTier> oCurrentTier = saleTierRepository.findTierAtDate(blockTime);

        while (oCurrentTier.isPresent() && usdRemaining.compareTo(BigDecimal.ZERO) > 0) {
            SaleTier currentTier = oCurrentTier.get();

            BigDecimal tokensDecimal = TokenUtils.convertUsdToTokenUnits(usdRemaining, appConfig.getUsdPerToken(),
                    currentTier.getDiscount());
            BigInteger tokensInteger = tokensDecimal.toBigInteger();

            if (currentTier.isAmountOverflowingTier(tokensInteger)) {
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

    private void convert(BigDecimal usd, Date blockTime) throws TokenCapOverflowException {
        Optional<SaleTier> tier = saleTierRepository.findTierAtDate(blockTime);
        if (tier.isPresent()) {
            if (tier.get().isFull()) {
                distributeToNextTier(usd, tier.get(), blockTime);
            } else {
                distributeToTier(usd, tier.get(), blockTime);
            }
        } else {
            throw new TokenCapOverflowException(BigInteger.ZERO, usd);
        }
    }

    private BigInteger distributeToTier(BigDecimal usd, SaleTier tier, Date blockTime) throws TokenCapOverflowException {
        BigDecimal tokensDecimal = TokenUtils.convertUsdToTokenUnits(usd, appConfig.getUsdPerToken(), tier.getDiscount());
        BigInteger tokensInteger = tokensDecimal.toBigInteger();

        if (tier.isAmountOverflowingTier(tokensInteger)) {
            tokensInteger = tier.getRemainingTokens();
            BigDecimal overflowInTokens = tokensDecimal.subtract(new BigDecimal(tokensInteger));
            BigDecimal overflowInUsd = TokenUtils.convertTokenUnitsToUsd(overflowInTokens, appConfig.getUsdPerToken(), tier.getDiscount());
            tier.setTokensSold(tier.getTokenMax());
            if (tier.hasDynamicDuration()) {
                tier.setEndDate(blockTime);
                adaptDatesOfFollowingTiers(tier, blockTime);
            }
            tier = saleTierRepository.save(tier);
            prepareNextTier(getNextTier(tier));
            try {
                tokensInteger = tokensInteger.add(distributeToNextTier(overflowInUsd, tier, blockTime));
            } catch (TokenCapOverflowException e) {
                e.addConvertedTokens(tokensInteger);
                throw e;
            }
        } else {
            tier.setTokensSold(tier.getTokensSold().add(tokensInteger));
            if (tier.isFull() && tier.hasDynamicDuration()) {
                tier.setEndDate(blockTime);
                adaptDatesOfFollowingTiers(tier, blockTime);
            }
            saleTierRepository.save(tier);
        }
        return tokensInteger;
    }

    private Optional<SaleTier> getNextTier(SaleTier tier) {
        return saleTierRepository.findByTierNo(tier.getTierNo() + 1);
    }

    private void prepareNextTier(Optional<SaleTier> tier) {
        if (tier.isPresent() && tier.get().hasDynamicMax()) {
            // TODO [claude, 09.07.18] set max token amount accordig to overall token max and tokens sold on previous
            // tiers.
        }
    }

    private void adaptDatesOfFollowingTiers(SaleTier tier, Date blockTime) {
        // TODO [claude, 09.07.18] set start and end dates of all following tiers, given the blocktime.
    }

    private BigInteger distributeToNextTier(BigDecimal usd, SaleTier previousTier, Date blockTime) throws TokenCapOverflowException {
        Optional<SaleTier> tier = saleTierRepository.findByTierNo(previousTier.getTierNo() + 1);
        if (tier.isPresent()) {
            return distributeToTier(usd, tier.get(), blockTime);
        } else {
            throw new TokenCapOverflowException(BigInteger.ZERO, usd);
        }
    }
}

