package io.iconator.monitor.token;

import java.math.BigDecimal;

public enum TokenUnit {

    MAIN(18),
    SMALLEST(1);

    private BigDecimal unitFactor;

    TokenUnit(int exponent) {
        this.unitFactor = BigDecimal.TEN.pow(exponent);
    }

    public BigDecimal getUnitFactor() {
        return unitFactor;
    }
}
