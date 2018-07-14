package io.iconator.monitor.token;

import java.math.BigDecimal;

public enum TokenUnit {

    /**
     * main token unit
     */
    TOKEN(18),
    /**
     * atomic token unit
     */
    TOMIC(1);

    private BigDecimal tomicFactor;

    TokenUnit(int exponent) {
        this.tomicFactor = BigDecimal.TEN.pow(exponent);
    }

    public BigDecimal getTomicFactor() {
        return tomicFactor;
    }
}
