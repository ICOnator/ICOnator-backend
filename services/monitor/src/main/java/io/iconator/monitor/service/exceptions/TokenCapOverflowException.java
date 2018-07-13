package io.iconator.monitor.service.exceptions;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TokenCapOverflowException extends Exception {

    private BigInteger convertedTokens;
    private BigDecimal overflow;

    public TokenCapOverflowException(BigInteger convertedTokens, BigDecimal overflow) {
        this.convertedTokens = convertedTokens;
        this.overflow = overflow;
    }

    private TokenCapOverflowException() {

    }

    public BigDecimal getOverflow() {
        return overflow;
    }

    public BigInteger getConvertedTokens() {
        return convertedTokens;
    }

    public void addConvertedTokens(BigInteger tokens) {
        convertedTokens = convertedTokens.add(tokens);
    }

    public void addOverflow(BigDecimal overflow) {
        this.overflow = this.overflow.add(overflow);
    }
}
