package io.iconator.commons.bitcoin;

import io.iconator.commons.bitcoin.exception.BitcoinUnitConversionNotImplementedException;
import org.bitcoinj.core.Coin;

import java.math.BigDecimal;

public class BitcoinUnitConverter {

    public static BigDecimal convert(BigDecimal value, BitcoinUnit unitFrom, BitcoinUnit unitTo)
            throws BitcoinUnitConversionNotImplementedException {
        if (unitFrom.equals(BitcoinUnit.SATOSHI) && unitTo.equals(BitcoinUnit.COIN)) {
            return new BigDecimal(Coin.valueOf(value.longValueExact()).getValue());
        }
        throw new BitcoinUnitConversionNotImplementedException(
                String.format("Converting %s to %s is not implemented.", unitFrom, unitTo));
    }

}
