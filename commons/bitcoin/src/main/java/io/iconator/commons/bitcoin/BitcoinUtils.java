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

    public static boolean isPending(Transaction tx) {
        return tx.getConfidence().getConfidenceType().equals(ConfidenceType.PENDING);
    }

    public static boolean isUnknown(Transaction tx) {
        return tx.getConfidence().getConfidenceType().equals(ConfidenceType.UNKNOWN);
    }

    public static boolean isBuilding(Transaction tx) {
        return tx.getConfidence().getConfidenceType().equals(ConfidenceType.BUILDING);
    }

    public static String getTransactionOutputIdentifier(TransactionOutput txo) {
        if (txo != null) {
            Transaction transaction = txo.getParentTransaction();
            if (transaction != null) {
                return transaction.getHashAsString() + "_" + String.valueOf(txo.getIndex());
            } else {
                throw new IllegalArgumentException("Transaction Output's parent transaction was null.");
            }
        }
        throw new IllegalArgumentException("Transaction Output was null.");
    }

    /*
     * Retrieve the timestamp from the first block that this transaction was seen in.
     */
    public static Date getTimestampOfTransaction(Transaction tx, SPVBlockStore blockStore) {
        return tx.getAppearsInHashes().keySet().stream()
                .map((blockHash) -> {
                    try {
                        return blockStore.get(blockHash);
                    } catch (BlockStoreException e) {
                        return null; // This can happen if the transaction was seen in a side-chain
                    }
                })
                .filter(Objects::nonNull)
                .map(StoredBlock::getHeader)
                .map(Block::getTime)
                .min(Date::compareTo).get();
    }

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
