package io.iconator.commons.bitcoin;

import java.math.BigDecimal;

/**
 * Unit definition based on: https://en.bitcoin.it/wiki/Units
 */
public enum BitcoinUnit {

    COIN(100_000_000),
    SATOSHI(1);

    private BigDecimal satoshiFactor;

    BitcoinUnit(int factor) {
        this.satoshiFactor = new BigDecimal(factor);
    }

    public BigDecimal getSatoshiFactor() {
        return satoshiFactor;
    }
}
