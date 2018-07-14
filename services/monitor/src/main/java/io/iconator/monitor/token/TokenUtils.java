package io.iconator.monitor.token;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class TokenUtils {

    /**
     * @param usd the USD amount to convert to tokens.
     * @param usdPerToken the USD price of a token.
     * @param discount the discount applied to the price of a token. E.g. with 0.25 the price for
     *                 one token is reduced to 75% of the original price.
     * @return the amount of tokens (in their atomic unit) worth the given USD amount
     */
    public static BigDecimal convertUsdToTomics(BigDecimal usd, BigDecimal usdPerToken, BigDecimal discount) {
        BigDecimal discountedUsdPerToken = usdPerToken.multiply(BigDecimal.ONE.subtract(discount));
        BigDecimal tokens = usd.divide(discountedUsdPerToken, MathContext.DECIMAL128);
        return TokenUnitConverter.convert(tokens, TokenUnit.TOKEN, TokenUnit.TOMIC);
    }

    /**
     * @param tomics the amount of tokens to convert given in their atomic unit.
     * @param usdPerToken the USD price of a token.
     * @param discount the discount applied to the price of a token. E.g. with 0.25 the price for
     *                 one token is reduced to 75% of the original price.
     * @return the price in USD for the given amount of atomic tokens.
     */
    public static BigDecimal convertTomicsToUsd(BigDecimal tomics, BigDecimal usdPerToken, BigDecimal discount) {
        BigDecimal tokensMainUnit = TokenUnitConverter.convert(tomics, TokenUnit.TOMIC, TokenUnit.TOKEN);
        BigDecimal discountedUsdPerToken = usdPerToken.multiply(BigDecimal.ONE.subtract(discount));
        return tokensMainUnit.multiply(discountedUsdPerToken);
    }

    /**
     * @param tomics the amount of tokens to convert given in their atomic unit.
     * @param usdPerToken the USD price of a token.
     * @param discount the discount applied to the price of a token. E.g. with 0.25 the price for
     *                 one token is reduced to 75% of the original price.
     * @return the price in USD for the given amount of atomic tokens.
     */
    public static BigDecimal convertTomicsToUsd(BigInteger tomics, BigDecimal usdPerToken, BigDecimal discount) {
        return convertTomicsToUsd(new BigDecimal(tomics), usdPerToken, discount);
    }
}
