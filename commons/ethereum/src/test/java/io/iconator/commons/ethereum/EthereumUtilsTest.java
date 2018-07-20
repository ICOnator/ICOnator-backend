package io.iconator.commons.ethereum;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class EthereumUtilsTest {

    @Test
    public void testUsdToSatoshiConversion() {
        BigDecimal usdPerEth = new BigDecimal("420.593483");

        BigDecimal usd = new BigDecimal("1");
        BigInteger wei = EthereumUtils.convertUsdToWei(usd, usdPerEth);
        assertEquals(0, wei.compareTo(new BigInteger("2377592712248468")));

        usd = new BigDecimal(1_000_000_000);
        wei = EthereumUtils.convertUsdToWei(usd, usdPerEth);
        assertEquals(0, wei.compareTo(new BigInteger("2377592712248468196070455")));

        usd = new BigDecimal("0.000000000000001");
        wei = EthereumUtils.convertUsdToWei(usd, usdPerEth);
        assertEquals(0, wei.compareTo(new BigInteger("2")));

        usd = new BigDecimal("0.0000000000000001");
        wei = EthereumUtils.convertUsdToWei(usd, usdPerEth);
        assertEquals(0, wei.compareTo(BigInteger.ZERO));
    }
}
