package io.iconator.monitor;

import org.junit.Test;

import java.math.BigDecimal;

public class DecimalTest {

    @Test
    public void test() {
        BigDecimal x = new BigDecimal("0.10001");
        BigDecimal y = new BigDecimal("10");
        BigDecimal z = x.multiply(y);

        x = new BigDecimal("12");
    }
}
