package io.iconator.commons.ethereum;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class EthereumUtilsTest {

    @Test
    public void testUsdToSatoshiConversion() {
        BigDecimal fiatPerEther = new BigDecimal("420.593483");

        BigDecimal fiatAmount = new BigDecimal("1");
        BigInteger wei = EthereumUtils.convertFiatToWei(fiatAmount, fiatPerEther);
        assertEquals(0, wei.compareTo(new BigInteger("2377592712248468")));

        fiatAmount = new BigDecimal(1_000_000_000);
        wei = EthereumUtils.convertFiatToWei(fiatAmount, fiatPerEther);
        assertEquals(0, wei.compareTo(new BigInteger("2377592712248468196070455")));

        fiatAmount = new BigDecimal("0.000000000000001");
        wei = EthereumUtils.convertFiatToWei(fiatAmount, fiatPerEther);
        assertEquals(0, wei.compareTo(new BigInteger("2")));

        fiatAmount = new BigDecimal("0.0000000000000001");
        wei = EthereumUtils.convertFiatToWei(fiatAmount, fiatPerEther);
        assertEquals(0, wei.compareTo(BigInteger.ZERO));
    }
}
