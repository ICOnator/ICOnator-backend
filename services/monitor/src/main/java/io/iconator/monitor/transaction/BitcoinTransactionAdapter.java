package io.iconator.monitor.transaction;

import io.iconator.commons.model.CurrencyType;
import org.bitcoinj.core.*;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Objects;

public class BitcoinTransactionAdapter extends BaseTransactionAdapter {

    private static final String BLOCKCHAIN_INFO_LINK  = "https://etherscan.io/tx/";

    private TransactionOutput bitcoinjTxOutput;
    private NetworkParameters bitcoinjNetworkParameters;
    private BlockStore bitcoinjBlockStore;

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

    public BitcoinTransactionAdapter(@NotNull TransactionOutput bitcoinjTxOutput,
                                     @NotNull NetworkParameters bitcoinjNetworkParameters,
                                     @NotNull BlockStore bitcoinjBlockStore) {
        this.bitcoinjTxOutput = bitcoinjTxOutput;
        this.bitcoinjNetworkParameters = bitcoinjNetworkParameters;
        this.bitcoinjBlockStore = bitcoinjBlockStore;
        this.blockHeight = null;
        this.blockTime = null;
    }

    @Override
    public String getTransactionId() {
        Transaction transaction = bitcoinjTxOutput.getParentTransaction();
        if (transaction != null) {
            return transaction.getHashAsString() + "_" + String.valueOf(bitcoinjTxOutput.getIndex());
        } else {
            throw new IllegalStateException("TransactionAdapter Output's parent transaction was null.");
        }
    }

    @Override
    public BigInteger getTransactionValue() {
        return BigInteger.valueOf(bitcoinjTxOutput.getValue().getValue());
    }

    @Override
    public BigDecimal getTransactionValueInMainUnit() {
        return null;
    }

    @Override
    public String getReceivingAddress() {
        return bitcoinjTxOutput
                .getAddressFromP2PKHScript(this.bitcoinjNetworkParameters)
                .toBase58();
    }

    @Override
    public Long getAssociatedInvestorId() {
        return getInvestorRepository()
                .findOptionalByPayInBitcoinAddress(getReceivingAddress())
                .get().getId();
    }

    @Override
    public Long getBlockHeight() {
        if (blockHeight == null) {
            blockHeight = bitcoinjTxOutput.getParentTransaction()
                    .getAppearsInHashes().keySet().stream()
                    .map((blockHash) -> {
                        try {
                            return bitcoinjBlockStore.get(blockHash);
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
            blockTime = bitcoinjTxOutput.getParentTransaction()
                    .getAppearsInHashes().keySet().stream()
                    .map((blockHash) -> {
                        try {
                            return bitcoinjBlockStore.get(blockHash);
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

    @Override
    public String getWebLinkToTransaction() {
        return BLOCKCHAIN_INFO_LINK + bitcoinjTxOutput.getParentTransaction().getHashAsString();
    }

    public Transaction getBitcoinjTransaction() {
        return bitcoinjTxOutput.getParentTransaction();
    }
}
