package io.iconator.commons.bitcoin;


import io.iconator.commons.bitcoin.exception.BitcoinUnitConversionNotImplementedException;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class BitcoinUnitConverterTest {

    @Test
    public void testSatoshiToBitcoinConversion() {
        BigDecimal coin = BigDecimal.ZERO;
        try {
            coin = BitcoinUnitConverter.convert(
                    BigDecimal.valueOf(100_000_099), BitcoinUnit.SATOSHI, BitcoinUnit.COIN);
        } catch (BitcoinUnitConversionNotImplementedException e) {
            fail();
        }
        assertEquals(0, coin.compareTo(new BigDecimal("1.00000099")));
    }

    @Test
    public void testBitcoinToSatoshiConversion() {
        BigDecimal satoshi = BigDecimal.ZERO;
        try {
            satoshi = BitcoinUnitConverter.convert(
                    new BigDecimal("1.00000099"), BitcoinUnit.COIN, BitcoinUnit.SATOSHI);
        } catch (BitcoinUnitConversionNotImplementedException e) {
            fail();
        }
        assertEquals(0, satoshi.compareTo(new BigDecimal(100_000_099)));
    }
}
