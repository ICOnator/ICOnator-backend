package io.iconator.commons.ethereum;

import io.iconator.commons.ethereum.exception.EthereumUnitConversionNotImplementedException;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class EthereumUnitCovnerterTest {

    @Test
    public void testWeiToEtherConversion() {
        BigDecimal ether = BigDecimal.ZERO;
        try {
            ether = EthereumUnitConverter.convert(
                    new BigDecimal("1000000000000000099.82"), EthereumUnit.WEI, EthereumUnit.ETHER);
        } catch (EthereumUnitConversionNotImplementedException e) {
            fail();
        }
        assertEquals(0, ether.compareTo(new BigDecimal("1.00000000000000009982")));
    }

    @Test
    public void testEtherToWeiConversion() {
        BigDecimal wei = BigDecimal.ZERO;
        try {
            wei = EthereumUnitConverter.convert(
                    new BigDecimal("1.82239000000000000083"), EthereumUnit.ETHER, EthereumUnit.WEI);
        } catch (EthereumUnitConversionNotImplementedException e) {
            fail();
        }
        assertEquals(0, wei.compareTo(new BigDecimal("1822390000000000000.83")));
    }
}
