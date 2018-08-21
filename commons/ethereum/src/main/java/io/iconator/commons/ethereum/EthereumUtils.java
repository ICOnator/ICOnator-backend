package io.iconator.commons.ethereum;

import io.iconator.commons.ethereum.exception.EthereumUnitConversionNotImplementedException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class EthereumUtils {

    // TODO This is only used in tests but no productive code.
    public static BigInteger convertUsdToWei(BigDecimal usd, BigDecimal usdPerEth) {
        BigDecimal ethers = usd.divide(usdPerEth, MathContext.DECIMAL128);
        try {
            return EthereumUnitConverter.convert(ethers, EthereumUnit.ETHER, EthereumUnit.WEI).toBigInteger();
        } catch (EthereumUnitConversionNotImplementedException e) {
            throw new RuntimeException(e);
        }
    }
}
