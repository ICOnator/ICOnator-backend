package io.iconator.commons.model;

import java.math.BigDecimal;

public enum CurrencyType {

    // Fiat:
    USD("USD", 2),
    CHF("CHF", 2),

    // Crypto Currency:
    ETH("ETH", 18),
    BTC("BTC", 8),
    ERC20("ERC20");

    private final String code;
    private BigDecimal atomicUnitFactor;

    CurrencyType(String code, int exponent) {
        this.code = code;
        this.atomicUnitFactor = BigDecimal.TEN.pow(exponent);
    }

    CurrencyType(String code) {
        this.code = code;
        this.atomicUnitFactor = null;
    }

    public BigDecimal getAtomicUnitFactor() {
        return atomicUnitFactor;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return getCode();
    }
}
