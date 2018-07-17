package io.iconator.monitor.service;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import io.iconator.commons.db.services.SaleTierService;
import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.monitor.config.MonitorAppConfig;
import io.iconator.monitor.service.exceptions.TokenCapOverflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.github.rholder.retry.StopStrategies.neverStop;
import static com.github.rholder.retry.WaitStrategies.randomWait;

@Service
public class TokenConversionService {

    private final static Logger LOG = LoggerFactory.getLogger(TokenConversionService.class);

    @Autowired
    private SaleTierRepository saleTierRepository;

    @Autowired
    private SaleTierService saleTierService;

    @Autowired
    private MonitorAppConfig appConfig;

    /**
     * @param usd         the USD amount to convertAndDistributeToTiers to tokens.
     * @param discount    the discount applied to the price of a token. E.g. with 0.25 the price for
     *                    one token is reduced to 75% of the original price.
     * @return the amount of tokens (in their atomic unit) worth the given USD amount
     */
    public BigDecimal convertUsdToTomics(BigDecimal usd, BigDecimal discount) {
        BigDecimal discountedUsdPerToken = appConfig.getUsdPerToken().multiply(BigDecimal.ONE.subtract(discount));
        BigDecimal tokens = usd.divide(discountedUsdPerToken, MathContext.DECIMAL128);
        return convertTokensToTomics(tokens);
    }

    /**
     * @param tomics      the amount of tokens to convertAndDistributeToTiers given in their atomic unit.
     * @param discount    the discount applied to the price of a token. E.g. with 0.25 the price for
     *                    one token is reduced to 75% of the original price.
     * @return the price in USD for the given amount of atomic tokens.
     */
    public BigDecimal convertTomicsToUsd(BigDecimal tomics, BigDecimal discount) {
        BigDecimal tokens = convertTomicsToTokens(tomics);
        BigDecimal discountedUsdPerToken = appConfig.getUsdPerToken().multiply(BigDecimal.ONE.subtract(discount));
        return tokens.multiply(discountedUsdPerToken);
    }

    /**
     * @param tomics      the amount of tokens to convertAndDistributeToTiers given in their atomic unit.
     * @param discount    the discount applied to the price of a token. E.g. with 0.25 the price for
     *                    one token is reduced to 75% of the original price.
     * @return the price in USD for the given amount of atomic tokens.
     */
    public BigDecimal convertTomicsToUsd(BigInteger tomics, BigDecimal discount) {
        return convertTomicsToUsd(new BigDecimal(tomics), discount);
    }

    public BigDecimal convertTomicsToTokens(BigInteger tomics) {
        return new BigDecimal(tomics.divide(appConfig.getAtomicUnitFactor()));
    }

    public BigDecimal convertTomicsToTokens(BigDecimal tomics) {
        return tomics.divide(new BigDecimal(appConfig.getAtomicUnitFactor()), MathContext.DECIMAL128);
    }

    public BigDecimal convertTokensToTomics(BigDecimal value) {
        return value.multiply(new BigDecimal(appConfig.getAtomicUnitFactor()));
    }

    public BigInteger convertAndDistributeToTiersWithRetries(BigDecimal usd, Date blockTime) throws Throwable {
        if (blockTime == null) throw new IllegalArgumentException("Block time must not be null.");
        if (usd == null) throw new IllegalArgumentException("USD amount must not be null.");

        // Retry as long as there are database locking exceptions.
        Retryer<BigInteger> retryer = RetryerBuilder.<BigInteger>newBuilder()
                .retryIfExceptionOfType(OptimisticLockingFailureException.class)
                .retryIfExceptionOfType(OptimisticLockException.class)
                .withWaitStrategy(randomWait(appConfig.getTokenConversionMaxTimeWait(), TimeUnit.MILLISECONDS))
                .withStopStrategy(neverStop())
                .build();

        try {
            return retryer.call(() -> convertAndDistributeToTiers(usd, blockTime));
        } catch (ExecutionException | RetryException e) {
            if (!(e.getCause() instanceof TokenCapOverflowException)) {
                LOG.error("Currency to token conversion failed.", e);
            }
            throw e.getCause();
        }
    }

    // TODO [claude, 10.07.18]: Is it necessary to have a @Version on the SaleTiers if the isolation level is repeatable_reads?
    // Or vice-versa, is it necessary to have isolation of repeatable_reads if we have a Version on the sale tiers.
    @Transactional(rollbackFor = Exception.class, noRollbackFor = TokenCapOverflowException.class, isolation = Isolation.REPEATABLE_READ)
    public BigInteger convertAndDistributeToTiers(BigDecimal usd, Date blockTime) throws TokenCapOverflowException {
        Optional<SaleTier> oTier = saleTierService.getTierAtDate(blockTime);
        if (oTier.isPresent()) {
            handleDynamicMax(oTier.get());
            if (oTier.get().isFull()) {
                return distributeToNextTier(usd, oTier, blockTime);
            } else {
                return distributeToTier(usd, oTier.get(), blockTime);
            }
        } else {
            throw new TokenCapOverflowException(BigInteger.ZERO, usd);
        }
    }

    private BigInteger distributeToTier(BigDecimal usd, SaleTier tier, Date blockTime) throws TokenCapOverflowException {
        BigDecimal tomicsDecimal = convertUsdToTomics(usd, tier.getDiscount());
        BigInteger tomicsInteger = tomicsDecimal.toBigInteger();

        if (tier.isAmountOverflowingTier(tomicsInteger)) {
            tomicsInteger = tier.getRemainingTomics();
            BigDecimal overflowInTomics = tomicsDecimal.subtract(new BigDecimal(tomicsInteger));
            BigDecimal overflowInUsd = convertTomicsToUsd(overflowInTomics, tier.getDiscount());
            try {
                checkAndThrowOverflowingOverallTokenMax(tier, tomicsInteger);
            } catch (TokenCapOverflowException e) {
                e.addOverflow(overflowInUsd);
                throw e;
            }
            tier.setTomicsSold(tier.getTomicsMax());
            tier = saleTierRepository.save(tier);
            if (tier.hasDynamicDuration()) shiftDates(tier, blockTime);
            try {
                tomicsInteger = tomicsInteger.add(
                        distributeToNextTier(overflowInUsd, saleTierService.getNextTier(tier), blockTime));
            } catch (TokenCapOverflowException e) {
                e.addConvertedTomics(tomicsInteger);
                throw e;
            }
        } else {
            checkAndThrowOverflowingOverallTokenMax(tier, tomicsInteger);
            tier.setTomicsSold(tier.getTomicsSold().add(tomicsInteger));
            if (tier.isFull()) {
                if (tier.hasDynamicDuration()) shiftDates(tier, blockTime);
                saleTierService.getNextTier(tier).ifPresent(this::handleDynamicMax);
            }
            saleTierRepository.save(tier);
        }
        return tomicsInteger;
    }

    private void checkAndThrowOverflowingOverallTokenMax(SaleTier tier, BigInteger tomics) throws TokenCapOverflowException {
        if (isOverflowingOverallMax(tomics)) {
            BigInteger remainingTomics = getOverallRemainingTomics();
            BigInteger overflowInTomics = tomics.subtract(remainingTomics);
            BigDecimal overflowInUsd = convertTomicsToUsd(overflowInTomics, tier.getDiscount());
            tier.setTomicsSold(remainingTomics);
            saleTierRepository.save(tier);
            throw new TokenCapOverflowException(remainingTomics, overflowInUsd);
        }
    }

    private BigInteger getOverallRemainingTomics() {
        return getOverallTomicsAmount().subtract(saleTierService.getOverallTomicsSold());
    }

    private boolean isOverflowingOverallMax(BigInteger tomics) {
        return tomics.compareTo(getOverallRemainingTomics()) > 0;
    }

    private void shiftDates(SaleTier tier, Date blockTime) {
        long dateShift = tier.getEndDate().getTime() - blockTime.getTime();
        tier.setEndDate(blockTime);
        tier = saleTierRepository.save(tier);
        saleTierService.getAllFollowingTiers(tier).forEach(t -> {
            t.setStartDate(new Date(t.getStartDate().getTime() - dateShift));
            t.setEndDate(new Date(t.getEndDate().getTime() - dateShift));
            saleTierRepository.save(t);
        });
    }

    private BigInteger distributeToNextTier(BigDecimal usd, Optional<SaleTier> oTier, Date blockTime) throws TokenCapOverflowException {
        SaleTier tier = oTier.orElseThrow(() -> new TokenCapOverflowException(BigInteger.ZERO, usd));
        handleDynamicMax(tier);
        return distributeToTier(usd, tier, blockTime);
    }

    private void handleDynamicMax(SaleTier tier) {
        if (tier.hasDynamicMax() && (tier.getTomicsMax().compareTo(BigInteger.ZERO) == 0
                || tier.getTomicsMax() == null)) {
            tier.setTomicsMax(getOverallRemainingTomics());
            saleTierRepository.save(tier);
        }
    }

    private BigInteger getOverallTomicsAmount() {
        return convertTokensToTomics(appConfig.getOverallTokenAmount())
                .toBigInteger();
    }
}

