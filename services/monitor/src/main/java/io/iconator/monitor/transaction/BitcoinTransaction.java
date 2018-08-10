package io.iconator.monitor.transaction;

import io.iconator.commons.model.CurrencyType;
import org.bitcoinj.core.*;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.Date;
import java.util.Objects;

public class BitcoinTransaction implements io.iconator.monitor.transaction.Transaction {

    private TransactionOutput bitcoinTxOutput;
    private NetworkParameters bitcoinNetworkParameters;
    private BlockStore blockStore;

    /**
     * Remembering block height after first time getter is called. This is to save
     * computation time since without this the bitcoinj block store would be
     * searched every time the block height is requested.
     */
    private Long blockHeight;

    /**
     * Remembering block time after first time getter is called. This is to save
     * computation time since without this the bitcoinj block store would be
     * searched every time the block time is requested.
     */
    private Date blockTime;

    public BitcoinTransaction(@NotNull TransactionOutput bitcoinTxOutput,
                              @NotNull NetworkParameters bitcoinNetworkParameters,
                              @NotNull BlockStore blockStore) {
        this.bitcoinTxOutput = bitcoinTxOutput;
        this.bitcoinNetworkParameters = bitcoinNetworkParameters;
        this.blockStore = blockStore;
        this.blockHeight = null;
        this.blockTime = null;
    }

    @Override
    public String getTransactionId() {
        Transaction transaction = bitcoinTxOutput.getParentTransaction();
        if (transaction != null) {
            return transaction.getHashAsString() + "_" + String.valueOf(bitcoinTxOutput.getIndex());
        } else {
            throw new IllegalStateException("Transaction Output's parent transaction was null.");
        }
    }

    @Override
    public BigInteger getTransactionValue() {
        return BigInteger.valueOf(bitcoinTxOutput.getValue().getValue());
    }

    @Override
    public String getReceivingAddress() {
        return bitcoinTxOutput.getAddressFromP2PKHScript(this.bitcoinNetworkParameters).toBase58();
    }

    @Override
    public Long getBlockHeight() {
        if (blockHeight == null) {
            blockHeight = bitcoinTxOutput.getParentTransaction()
                    .getAppearsInHashes().keySet().stream()
                    .map((blockHash) -> {
                        try {
                            return blockStore.get(blockHash);
                        } catch (BlockStoreException e) {
                            // This can happen if the transaction was seen in a side-chain
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .map(StoredBlock::getHeight)
                    .min(Integer::compareTo).get().longValue();
            return blockHeight;
        } else {
            return blockHeight;
        }
    }

    @Override
    public CurrencyType getCurrencyType() {
        return CurrencyType.BTC;
    }

    /*
     * Retrieve the timestamp from the first block that the transaction was seen in.
     */
    @Override
    public Date getBlockTime() {
        if (blockTime == null) {
            blockTime = bitcoinTxOutput.getParentTransaction()
                    .getAppearsInHashes().keySet().stream()
                    .map((blockHash) -> {
                        try {
                            return blockStore.get(blockHash);
                        } catch (BlockStoreException e) {
                            // This can happen if the transaction was seen in a side-chain
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .map(StoredBlock::getHeader)
                    .map(Block::getTime)
                    .min(Date::compareTo).get();
            return blockTime;
        } else {
            return blockTime;
        }
    }
}
