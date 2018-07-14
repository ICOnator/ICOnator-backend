package io.iconator.monitor.service.exceptions;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TokenCapOverflowException extends Exception {

    private BigInteger convertedTomics;
    private BigDecimal overflow;

    public TokenCapOverflowException(BigInteger convertedTomics, BigDecimal overflow) {
        this.convertedTomics = convertedTomics;
        this.overflow = overflow;
    }

    private TokenCapOverflowException() {

    }

    public BigDecimal getOverflow() {
        return overflow;
    }

    public BigInteger getConvertedTomics() {
        return convertedTomics;
    }

    public void addConvertedTomics(BigInteger tokens) {
        convertedTomics = convertedTomics.add(tokens);
    }

    public void addOverflow(BigDecimal overflow) {
        this.overflow = this.overflow.add(overflow);
    }
}
