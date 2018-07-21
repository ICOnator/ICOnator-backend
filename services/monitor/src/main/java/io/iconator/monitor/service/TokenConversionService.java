package io.iconator.monitor.service;

import io.iconator.commons.db.services.SaleTierService;
import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.monitor.config.MonitorAppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Date;
import java.util.Optional;

@Service
public class TokenConversionService {

    @Autowired
    private SaleTierRepository saleTierRepository;

    @Autowired
    public SaleTierService saleTierService;

    @Autowired
    private MonitorAppConfig appConfig;

    // TODO [claude, 2018-07-19], Remove as soon as concurrency tests are through.
//    @Autowired
//    private PlatformTransactionManager txManager;

    /**
     * @param usd      the USD amount to convert to tokens.
     * @param discount the discount applied to the price of a token. E.g. with 0.25 the price for
     *                 one token is reduced to 75% of the original price.
     * @return the amount of tokens (in their atomic unit) worth the given USD amount
     */
    public BigDecimal convertUsdToTomics(BigDecimal usd, BigDecimal discount) {
        BigDecimal discountedUsdPerToken = appConfig.getUsdPerToken().multiply(BigDecimal.ONE.subtract(discount));
        BigDecimal tokens = usd.divide(discountedUsdPerToken, MathContext.DECIMAL128);
        return convertTokensToTomics(tokens);
    }

    /**
     * @param tomics   the amount of tokens to convert given in their atomic unit.
     * @param discount the discount applied to the price of a token. E.g. with 0.25 the price for
     *                 one token is reduced to 75% of the original price.
     * @return the price in USD for the given amount of atomic tokens.
     */
    public BigDecimal convertTomicsToUsd(BigDecimal tomics, BigDecimal discount) {
        BigDecimal tokens = convertTomicsToTokens(tomics);
        BigDecimal discountedUsdPerToken = appConfig.getUsdPerToken().multiply(BigDecimal.ONE.subtract(discount));
        return tokens.multiply(discountedUsdPerToken);
    }

    /**
     * @param tomics   the amount of tokens to convert given in their atomic unit.
     * @param discount the discount applied to the price of a token. E.g. with 0.25 the price for
     *                 one token is reduced to 75% of the original price.
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

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.REPEATABLE_READ)
    public TokenDistributionResult convertAndDistributeToTiers(BigDecimal usd, Date blockTime) {
        // TODO [claude, 2018-07-19], Remove as soon as concurrency tests are through.
//        DefaultTransactionDefinition dtd = new DefaultTransactionDefinition();
//        dtd.setPropagationBehavior(PROPAGATION_MANDATORY);
//        TransactionStatus s = txManager.getTransaction(dtd);

        Optional<SaleTier> oTier = saleTierService.getTierAtDate(blockTime);
        if (oTier.isPresent()) {
            handleDynamicMax(oTier.get());
            if (oTier.get().isFull()) {
                return distributeToNextTier(usd, oTier, blockTime);
            } else {
                return distributeToTier(usd, oTier.get(), blockTime);
            }
        } else {
            return new TokenDistributionResult(BigInteger.ZERO, usd);
        }
    }

    private TokenDistributionResult distributeToTier(BigDecimal usd, SaleTier tier, Date blockTime) {
        // Remembering decimal value to have more precision in case a conversion back to usd is necessary because of an overflow.
        BigDecimal tomicsDecimal = convertUsdToTomics(usd, tier.getDiscount());
        BigInteger tomicsInteger = tomicsDecimal.toBigInteger();

        if (tier.isAmountOverflowingTier(tomicsInteger)) {
            BigInteger remainingTomicsOnTier = tier.getRemainingTomics();
            BigDecimal overflowOverTier = tomicsDecimal.subtract(new BigDecimal(remainingTomicsOnTier));
            BigDecimal overflowInUsd = convertTomicsToUsd(overflowOverTier, tier.getDiscount());
            if (isOverflowingTotalMax(remainingTomicsOnTier)) {
                return handleTotalMaxOverflow(tier, remainingTomicsOnTier).addToOverflow(overflowInUsd);
            } else {
                tier.setTomicsSold(tier.getTomicsMax());
                tier = saleTierRepository.save(tier);
                if (tier.hasDynamicDuration()) shiftDates(tier, blockTime);
                return distributeToNextTier(overflowInUsd, saleTierService.getSubsequentTier(tier), blockTime)
                        .addToDistributedTomics(remainingTomicsOnTier);
            }
        } else {
            if (isOverflowingTotalMax(tomicsInteger)) {
                return handleTotalMaxOverflow(tier, tomicsInteger);
            } else {
                tier.setTomicsSold(tier.getTomicsSold().add(tomicsInteger));
                if (tier.isFull()) {
                    if (tier.hasDynamicDuration()) shiftDates(tier, blockTime);
                    saleTierService.getSubsequentTier(tier).ifPresent(this::handleDynamicMax);
                }
                saleTierRepository.save(tier);
                return new TokenDistributionResult(tomicsInteger, BigDecimal.ZERO);
            }
        }
    }

    private TokenDistributionResult handleTotalMaxOverflow(SaleTier tier, BigInteger tomicsForTier) {
        TokenDistributionResult result = distributeTotalRemainingTokensToTier(tier);
        BigInteger totalMaxOverflow = tomicsForTier.subtract(result.getDistributedTomics());
        return result.addToOverflow(convertTomicsToUsd(totalMaxOverflow, tier.getDiscount()));
    }

    private TokenDistributionResult distributeTotalRemainingTokensToTier(SaleTier tier) {
        BigInteger totalRemainingTokens = getTotalRemainingTomics();
        tier.setTomicsSold(tier.getTomicsSold().add(totalRemainingTokens));
        saleTierRepository.save(tier);
        return new TokenDistributionResult(totalRemainingTokens, BigDecimal.ZERO);
    }

    private BigInteger getTotalRemainingTomics() {
        return getTotalTomicsAmount().subtract(saleTierService.getTotalTomicsSold());
    }

    private boolean isOverflowingTotalMax(BigInteger tomics) {
        return tomics.compareTo(getTotalRemainingTomics()) > 0;
    }

    private void shiftDates(SaleTier tier, Date blockTime) {
        long dateShift = tier.getEndDate().getTime() - blockTime.getTime();
        tier.setEndDate(blockTime);
        tier = saleTierRepository.save(tier);
        saleTierService.getAllSubsequentTiers(tier).forEach(t -> {
            t.setStartDate(new Date(t.getStartDate().getTime() - dateShift));
            t.setEndDate(new Date(t.getEndDate().getTime() - dateShift));
            saleTierRepository.save(t);
        });
    }

    private TokenDistributionResult distributeToNextTier(BigDecimal usd, Optional<SaleTier> oTier, Date blockTime) {
        if (oTier.isPresent()) {
            handleDynamicMax(oTier.get());
            return distributeToTier(usd, oTier.get(), blockTime);
        } else {
            return new TokenDistributionResult(BigInteger.ZERO, usd);
        }
    }

    private void handleDynamicMax(SaleTier tier) {
        if (tier.hasDynamicMax() && (tier.getTomicsMax().compareTo(BigInteger.ZERO) == 0
                || tier.getTomicsMax() == null)) {
            tier.setTomicsMax(getTotalRemainingTomics());
            saleTierRepository.save(tier);
        }
    }

    private BigInteger getTotalTomicsAmount() {
        return convertTokensToTomics(appConfig.getTotalTokenAmount())
                .toBigInteger();
    }

    public static class TokenDistributionResult {

        private BigInteger tomics;
        private BigDecimal overflow;

        public TokenDistributionResult(BigInteger tomics, BigDecimal overflow) {
            this.tomics = tomics;
            this.overflow = overflow;
        }

        public TokenDistributionResult() {
            this.tomics = BigInteger.ZERO;
            this.overflow = BigDecimal.ZERO;
        }

        public boolean hasOverflow() {
            return overflow.compareTo(BigDecimal.ZERO) > 0;
        }

        public BigDecimal getOverflow() {
            return overflow;
        }

        public BigInteger getDistributedTomics() {
            return tomics;
        }

        public TokenDistributionResult addToDistributedTomics(BigInteger tomics) {
            this.tomics = this.tomics.add(tomics);
            return this;
        }

        public TokenDistributionResult addToOverflow(BigDecimal overflow) {
            this.overflow = this.overflow.add(overflow);
            return this;
        }
    }
}

