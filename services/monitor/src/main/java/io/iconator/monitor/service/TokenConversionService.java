package io.iconator.monitor.service;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import io.iconator.commons.db.services.SaleTierService;
import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.monitor.config.MonitorAppConfig;
import io.iconator.monitor.service.exceptions.TokenCapOverflowException;
import io.iconator.monitor.token.TokenUnit;
import io.iconator.monitor.token.TokenUnitConverter;
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

    public BigInteger convertWithRetries(BigDecimal usd, Date blockTime) throws Throwable {
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
            return retryer.call(() -> convert(usd, blockTime));
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
    public BigInteger convert(BigDecimal usd, Date blockTime) throws TokenCapOverflowException {
        Optional<SaleTier> oTier = saleTierRepository.findTierAtDate(blockTime);
        if (oTier.isPresent()) {
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
        BigDecimal tokensDecimal = TokenUtils.convertUsdToTokens(usd, appConfig.getUsdPerToken(), tier.getDiscount());
        BigInteger tokensInteger = tokensDecimal.toBigInteger();

        if (tier.isAmountOverflowingTier(tokensInteger)) {
            tokensInteger = tier.getRemainingTokens();
            BigDecimal overflowInTokens = tokensDecimal.subtract(new BigDecimal(tokensInteger));
            BigDecimal overflowInUsd = TokenUtils.convertTokensToUsd(overflowInTokens, appConfig.getUsdPerToken(), tier.getDiscount());
            try {
                checkAndThrowOverflowingOverallTokenMax(tier, tokensInteger);
            } catch (TokenCapOverflowException e) {
                e.addOverflow(overflowInUsd);
                throw e;
            }
            tier.setTokensSold(tier.getTokenMax());
            tier = saleTierRepository.save(tier);
            if (tier.hasDynamicDuration()) shiftDates(tier, blockTime);
            try {
                tokensInteger = tokensInteger.add(
                        distributeToNextTier(overflowInUsd, saleTierService.getNextTier(tier), blockTime));
            } catch (TokenCapOverflowException e) {
                e.addConvertedTokens(tokensInteger);
                throw e;
            }
        } else {
            checkAndThrowOverflowingOverallTokenMax(tier, tokensInteger);
            tier.setTokensSold(tier.getTokensSold().add(tokensInteger));
            if (tier.isFull() && tier.hasDynamicDuration()) {
                shiftDates(tier, blockTime);
            }
            saleTierRepository.save(tier);
        }
        return tokensInteger;
    }

    private void checkAndThrowOverflowingOverallTokenMax(SaleTier tier, BigInteger tokensInteger) throws TokenCapOverflowException {
        if (isOverflowingOverallMax(tokensInteger)) {
            BigInteger remainingTomics = getOverallRemainingTomicsAmount();
            BigInteger overflowInTokens = tokensInteger.subtract(remainingTomics);
            BigDecimal overflowInUsd = TokenUtils.convertTokensToUsd(overflowInTokens,
                    appConfig.getUsdPerToken(), tier.getDiscount());
            tier.setTokensSold(remainingTomics);
            throw new TokenCapOverflowException(remainingTomics, overflowInUsd);
        }
    }

    private BigInteger getOverallRemainingTomicsAmount() {
        return getOverallTomicsAmount().subtract(saleTierService.getOverallTokenAmountSold());
    }

    private boolean isOverflowingOverallMax(BigInteger tomics) {
        return tomics.compareTo(getOverallRemainingTomicsAmount()) > 0;
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
        if (oTier.isPresent()) {
            SaleTier tier = oTier.get();
            if (tier.hasDynamicMax() && (tier.getTokenMax() == BigInteger.ZERO || tier.getTokenMax() == null)) {
                tier.setTokenMax(getOverallTomicsAmount().subtract(saleTierService.getOverallTokenAmountSold()));
                saleTierRepository.save(tier);
            }
            return distributeToTier(usd, tier, blockTime);
        } else {
            throw new TokenCapOverflowException(BigInteger.ZERO, usd);
        }
    }

    private BigInteger getOverallTomicsAmount() {
        return TokenUnitConverter.convert(appConfig.getOverallTokenAmount(), TokenUnit.MAIN, TokenUnit.SMALLEST)
                .toBigInteger();
    }
}

