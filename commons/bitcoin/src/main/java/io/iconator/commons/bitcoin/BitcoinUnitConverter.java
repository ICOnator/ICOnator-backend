package io.iconator.commons.bitcoin;

import io.iconator.commons.bitcoin.exception.BitcoinUnitConversionNotImplementedException;
import org.bitcoinj.core.Coin;

import java.math.BigDecimal;
import java.math.BigInteger;

public class BitcoinUnitConverter {

    public static BigDecimal convert(BigDecimal value, BitcoinUnit unitFrom, BitcoinUnit unitTo)
            throws BitcoinUnitConversionNotImplementedException {

        if (unitFrom.equals(BitcoinUnit.SATOSHI) && unitTo.equals(BitcoinUnit.COIN)) {
            return new BigDecimal(Coin.valueOf(value.longValueExact()).toPlainString());
        }
        if (unitFrom.equals(BitcoinUnit.COIN) && unitTo.equals(BitcoinUnit.SATOSHI)) {
            return value.multiply(BitcoinUnit.COIN.getSatoshiFactor());
        }
        throw new BitcoinUnitConversionNotImplementedException(
                String.format("Converting %s to %s is not implemented.", unitFrom, unitTo));
    }

    public static BigDecimal convert(BigInteger value, BitcoinUnit unitFrom, BitcoinUnit unitTo)
            throws BitcoinUnitConversionNotImplementedException {

        return convert(new BigDecimal(value), unitFrom, unitTo);
    }
}
