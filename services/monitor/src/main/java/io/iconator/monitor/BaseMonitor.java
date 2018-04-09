package io.iconator.monitor;

import io.iconator.commons.model.db.SaleTier;
import io.iconator.commons.sql.dao.SaleTierRepository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Optional;

public class BaseMonitor {

    private SaleTierRepository saleTierRepository;

    private static final MathContext mc = new MathContext(34, RoundingMode.DOWN);

    public BaseMonitor(SaleTierRepository saleTierRepository) {
        this.saleTierRepository = saleTierRepository;
    }

    /**
     * The returned pair consists of
     * - key:   the tokens that the given amount is worth
     * - value: amount which could not be converted into tokens because all tiers where already full.
     * <p>
     * Converts with the assumption of an exchange rate of 1:1 of the given input to the tokens
     * (smallest unit of the token).
     * <p>
     * Converted currency amounts are rounded down to the next integer token amount.
     */
    public ConversionResult calcTokensAndUpdateTiers(BigDecimal amount, Date blockTime) {
        // TODO 18-03-30 Claude:
        // Must be synchronized from here in order that a tier does not change while calculating
        // the amount of tokens a investor will receive.
        Optional<SaleTier> oCurrentTier = saleTierRepository.findByIsActiveTrue();
        BigDecimal remainingAmount = amount;
        BigInteger tokensTotal = BigInteger.ZERO;

        while (oCurrentTier.isPresent()) {
            SaleTier currentTier = oCurrentTier.get();
            if (!currentTier.isActive()) {
                currentTier.setStartDate(blockTime);
                currentTier.setActive();
            }

            if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal tentativeTokensDecimal = calcAmountInTokens(remainingAmount, currentTier.getDiscount());
            BigInteger tentativeTokens = tentativeTokensDecimal.toBigInteger();

            if (tokensExceedHardcap(tentativeTokens, currentTier)) {
                // Tokens must be distributed over multiple tiers
                BigInteger tokensToCurrentTier = currentTier.getTokenMax().subtract(currentTier.getTokensSold());
                currentTier.setTokensSold(tokensToCurrentTier);
                tokensTotal = tokensTotal.add(tokensToCurrentTier);
                remainingAmount = calcAmountInCurrency(
                        tentativeTokensDecimal.subtract(new BigDecimal(tokensToCurrentTier)),
                        currentTier.getDiscount());

                currentTier.setEndDate(blockTime);
                currentTier.setInactive();
                oCurrentTier = getNextTier(currentTier);
            } else {
                // All tokens can be retrieved from the same tier.
                currentTier.setTokensSold(currentTier.getTokensSold().add(tentativeTokens));
                tokensTotal = tokensTotal.add(tentativeTokens);
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

    private BigDecimal calcAmountInTokens(BigDecimal currency, BigDecimal discountRate) {
        return currency.divide(BigDecimal.ONE.subtract(discountRate), mc);
    }

    private BigDecimal calcAmountInCurrency(BigDecimal tokens, BigDecimal discountRate) {
        return tokens.multiply(BigDecimal.ONE.subtract(discountRate));
    }

    private boolean tokensExceedHardcap(BigInteger tokens, SaleTier tier) {
        return tier.getTokensSold().add(tokens).compareTo(tier.getTokenMax()) >= 0;
    }

//    public boolean isLastTierActiveAndAmountOverflowsLastTier(BigDecimal amountUsd) {
//        Optional<SaleTier> oLastTier = saleTierRepository.findFirstByOrderByEndDateDesc();
//        if (oLastTier.isPresent()) {
//            SaleTier lastTier = oLastTier.get();
//            BigInteger tokens = convertWithDiscountRate(amountUsd, lastTier.getDiscount()).toBigInteger();
//            boolean overFlowsLastTier = lastTier.getTokensSold().add(tokens).compareTo(lastTier.getTokenMax()) > 0;
//            return  lastTier.isActive() && overFlowsLastTier;
//        } else {
//            throw new IllegalStateException("No Tier was found in database.");
//        }
//    }
}
