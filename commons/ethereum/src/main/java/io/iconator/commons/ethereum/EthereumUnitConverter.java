package io.iconator.commons.ethereum;

import io.iconator.commons.ethereum.exception.EthereumUnitConversionNotImplementedException;
import org.web3j.utils.Convert;

import java.math.BigDecimal;

public class EthereumUnitConverter {

    // TODO This is only used in tests but no productive code.
    public static BigDecimal convert(BigDecimal value, EthereumUnit unitFrom, EthereumUnit unitTo)
            throws EthereumUnitConversionNotImplementedException {
        if (unitFrom.equals(EthereumUnit.WEI) && unitTo.equals(EthereumUnit.ETHER)) {
            return Convert.fromWei(value, Convert.Unit.ETHER);
        }
        if (unitFrom.equals(EthereumUnit.ETHER) && unitTo.equals(EthereumUnit.WEI)) {
            return Convert.toWei(value, Convert.Unit.ETHER);
        }
        throw new EthereumUnitConversionNotImplementedException(
                String.format("Converting %s to %s is not implemented.", unitFrom, unitTo)
        );
    }

}
