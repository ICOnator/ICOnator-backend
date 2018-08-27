package io.iconator.commons.bitcoin;

import io.iconator.commons.bitcoin.exception.BitcoinUnitConversionNotImplementedException;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence.ConfidenceType;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Date;
import java.util.Objects;

public class BitcoinUtils {

    public static BigDecimal convertSatoshiToUsd(BigInteger satoshi, BigDecimal USDperBTC) {
        try {
            return BitcoinUnitConverter.convert(satoshi, BitcoinUnit.SATOSHI, BitcoinUnit.COIN)
                    .multiply(USDperBTC);
        } catch (BitcoinUnitConversionNotImplementedException e) {
            throw new RuntimeException(e);
        }
    }

    public static BigInteger convertUsdToSatoshi(BigDecimal usd, BigDecimal USDperBTC) {
        try {
            BigDecimal coins = usd.divide(USDperBTC, MathContext.DECIMAL128);
            return BitcoinUnitConverter.convert(coins, BitcoinUnit.COIN, BitcoinUnit.SATOSHI)
                    .toBigInteger();
        } catch (BitcoinUnitConversionNotImplementedException e) {
            throw new RuntimeException(e);
        }
    }
}
