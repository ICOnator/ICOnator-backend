package io.iconator.monitor.token;

import io.iconator.monitor.token.exceptions.TokenUnitConversionNotImplementedException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class TokenUnitConverter {

    public static BigDecimal convert(BigDecimal value, TokenUnit unitFrom, TokenUnit unitTo)
            throws TokenUnitConversionNotImplementedException {

        if (unitFrom.equals(TokenUnit.SMALLEST) && unitTo.equals(TokenUnit.MAIN)) {
            return value.divide(TokenUnit.MAIN.getUnitFactor(), MathContext.DECIMAL128);
        }
        if (unitFrom.equals(TokenUnit.MAIN) && unitTo.equals(TokenUnit.SMALLEST)) {
            return value.multiply(TokenUnit.MAIN.getUnitFactor());
        }
        throw new TokenUnitConversionNotImplementedException(
                String.format("Converting %s to %s is not implemented.", unitFrom, unitTo));
    }

    public static BigDecimal convert(BigInteger value, TokenUnit unitFrom, TokenUnit unitTo)
            throws TokenUnitConversionNotImplementedException {

        return convert(new BigDecimal(value), unitFrom, unitTo);
    }
}
