package io.iconator.monitor.token;

import java.math.BigDecimal;
import java.math.MathContext;

// TODO [cmu, 07.06.18] Write Tests
public class TokenUtils {

    public static BigDecimal convertUsdToTokenUnits(BigDecimal usd, BigDecimal usdPerToken, BigDecimal discountRate) {
        BigDecimal discountedUsdPerToken = usdPerToken.multiply(BigDecimal.ONE.subtract(discountRate));
        BigDecimal tokens = usd.divide(discountedUsdPerToken, MathContext.DECIMAL128);
        return TokenUnitConverter.convert(tokens, TokenUnit.MAIN, TokenUnit.SMALLEST);
    }

    public static BigDecimal convertTokenUnitsToUsd(BigDecimal tokenUnits, BigDecimal usdPerToken, BigDecimal discountRate) {
        BigDecimal tokens = TokenUnitConverter.convert(tokenUnits, TokenUnit.SMALLEST, TokenUnit.MAIN);
        BigDecimal discountedUsdPerToken = usdPerToken.multiply(BigDecimal.ONE.subtract(discountRate));
        return tokens.multiply(discountedUsdPerToken);
    }
}
