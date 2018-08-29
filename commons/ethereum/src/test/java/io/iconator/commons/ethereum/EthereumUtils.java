package io.iconator.commons.ethereum;

import io.iconator.commons.ethereum.exception.EthereumUnitConversionNotImplementedException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class EthereumUtils {

    public static BigInteger convertFiatToWei(BigDecimal fiatAmount, BigDecimal fiatPerEther) {
        BigDecimal ethers = fiatAmount.divide(fiatPerEther, MathContext.DECIMAL128);
        try {
            return EthereumUnitConverter.convert(ethers, EthereumUnit.ETHER, EthereumUnit.WEI).toBigInteger();
        } catch (EthereumUnitConversionNotImplementedException e) {
            throw new RuntimeException(e);
        }
    }
}
