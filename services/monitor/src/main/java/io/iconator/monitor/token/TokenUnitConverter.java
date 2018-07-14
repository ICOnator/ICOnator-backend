package io.iconator.monitor.token;

import io.iconator.monitor.token.exceptions.TokenUnitConversionNotImplementedException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class TokenUnitConverter {

    public static BigDecimal convert(BigDecimal value, TokenUnit unitFrom, TokenUnit unitTo)
            throws TokenUnitConversionNotImplementedException {

        if (unitFrom.equals(TokenUnit.TOMIC) && unitTo.equals(TokenUnit.TOKEN)) {
            return value.divide(TokenUnit.TOKEN.getTomicFactor(), MathContext.DECIMAL128);
        }
        if (unitFrom.equals(TokenUnit.TOKEN) && unitTo.equals(TokenUnit.TOMIC)) {
            return value.multiply(TokenUnit.TOKEN.getTomicFactor());
        }
        throw new TokenUnitConversionNotImplementedException(
                String.format("Converting %s to %s is not implemented.", unitFrom, unitTo));
    }

    public static BigDecimal convert(BigInteger value, TokenUnit unitFrom, TokenUnit unitTo)
            throws TokenUnitConversionNotImplementedException {

        return convert(new BigDecimal(value), unitFrom, unitTo);
    }
}
