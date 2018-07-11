package io.iconator.monitor.token;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class TokenUtils {

    /**
     * @param usd the USD amount to convert to tokens.
     * @param usdPerToken the USD price of a token in its main unit.
     * @param discount the discount applied to the price of a token. E.g. with 0.25 the price for one token is reduced
     *                 to 75% of the original price.
     * @return the amount of tokens (in their atomic unit) worth the given USD amount
     */
    public static BigDecimal convertUsdToTokens(BigDecimal usd, BigDecimal usdPerToken, BigDecimal discount) {
        BigDecimal discountedUsdPerToken = usdPerToken.multiply(BigDecimal.ONE.subtract(discount));
        BigDecimal tokens = usd.divide(discountedUsdPerToken, MathContext.DECIMAL128);
        return TokenUnitConverter.convert(tokens, TokenUnit.MAIN, TokenUnit.SMALLEST);
    }

    /**
     * @param tokens the amount of tokens to convert given in their atomic unit.
     * @param usdPerToken the USD price of a token in its main unit.
     * @param discount the discount applied to the price of a token. E.g. with 0.25 the price for one token is reduced
     *                 to 75% of the original price.
     * @return the price in USD for the given amount of tokens.
     */
    public static BigDecimal convertTokensToUsd(BigDecimal tokens, BigDecimal usdPerToken, BigDecimal discount) {
        BigDecimal tokensMainUnit = TokenUnitConverter.convert(tokens, TokenUnit.SMALLEST, TokenUnit.MAIN);
        BigDecimal discountedUsdPerToken = usdPerToken.multiply(BigDecimal.ONE.subtract(discount));
        return tokensMainUnit.multiply(discountedUsdPerToken);
    }

    /**
     * @param tokens the amount of tokens to convert given in their atomic unit.
     * @param usdPerToken the USD price of a token in its main unit.
     * @param discount the discount applied to the price of a token. E.g. with 0.25 the price for one token is reduced
     *                 to 75% of the original price.
     * @return the price in USD for the given amount of tokens.
     */
    public static BigDecimal convertTokensToUsd(BigInteger tokens, BigDecimal usdPerToken, BigDecimal discount) {
        return convertTokensToUsd(new BigDecimal(tokens), usdPerToken, discount);
    }
}
