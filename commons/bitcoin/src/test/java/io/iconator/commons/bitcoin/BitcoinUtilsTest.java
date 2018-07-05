package io.iconator.commons.bitcoin;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class BitcoinUtilsTest {

    @Test
    public void testUsdToSatoshiConversion() {
        BigDecimal usdPerBtc = new BigDecimal("6010");

        BigDecimal usd = new BigDecimal("1");
        BigInteger satoshi = BitcoinUtils.convertUsdToSatoshi(usd, usdPerBtc);
        assertEquals(0, satoshi.compareTo(new BigInteger("16638")));

        usd = new BigDecimal(1_000_000_000);
        satoshi = BitcoinUtils.convertUsdToSatoshi(usd, usdPerBtc);
        assertEquals(0, satoshi.compareTo(new BigInteger("16638935108153")));

        usd = new BigDecimal("0.0001");
        satoshi = BitcoinUtils.convertUsdToSatoshi(usd, usdPerBtc);
        assertEquals(0, satoshi.compareTo(BigInteger.ONE));

        usd = new BigDecimal("0.00001");
        satoshi = BitcoinUtils.convertUsdToSatoshi(usd, usdPerBtc);
        assertEquals(0, satoshi.compareTo(BigInteger.ZERO));
    }

    @Test
    public void testSatoshiToUsdConversion() {
        BigDecimal usdPerBtc = new BigDecimal("6010");

        BigInteger satoshi = BigInteger.valueOf(522_222_222L);
        BigDecimal usd = BitcoinUtils.convertSatoshiToUsd(satoshi, usdPerBtc);
        assertEquals(0, usd.compareTo(new BigDecimal("31385.5555422")));
    }
}
