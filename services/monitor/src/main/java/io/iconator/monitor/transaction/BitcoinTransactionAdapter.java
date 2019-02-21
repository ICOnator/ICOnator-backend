package io.iconator.monitor.transaction;

import io.iconator.commons.db.services.InvestorService;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.Investor;
import io.iconator.monitor.transaction.exception.MissingTransactionInformationException;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.Date;
import java.util.Objects;

/**
 * Bitcoin-sepcific transaction class.
 */
public class BitcoinTransactionAdapter extends BaseTransactionAdapter {

    private static final String BLOCKCHAIN_INFO_LINK = "https://blockchain.info/tx/";

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
                                     @NotNull BlockStore bitcoinjBlockStore,
                                     @NotNull InvestorService investorService) {
        super(investorService);
        this.bitcoinjTxOutput = bitcoinjTxOutput;
        this.bitcoinjNetworkParameters = bitcoinjNetworkParameters;
        this.bitcoinjBlockStore = bitcoinjBlockStore;
        this.blockHeight = null;
        this.blockTime = null;
    }

    @Override
    public String getTransactionId() throws MissingTransactionInformationException {
        try {
            Transaction transaction = bitcoinjTxOutput.getParentTransaction();
            return transaction.getHashAsString() + "_" + String.valueOf(bitcoinjTxOutput.getIndex());
        } catch (Exception e) {
            throw new MissingTransactionInformationException("Couldn't fetch transaction id.", e);
        }
    }

    @Override
    public BigInteger getTransactionValue() throws MissingTransactionInformationException {
        try {
            return BigInteger.valueOf(bitcoinjTxOutput.getValue().getValue());
        } catch (Exception e) {
            throw new MissingTransactionInformationException("Couldn't fetch transaction value.", e);
        }
    }

    @Override
    public String getReceivingAddress() throws MissingTransactionInformationException {
        try {
            return bitcoinjTxOutput
                    .getAddressFromP2PKHScript(this.bitcoinjNetworkParameters)
                    .toBase58();
        } catch (Exception e) {
            throw new MissingTransactionInformationException("Couldn't fetch receiving address.", e);
        }
    }

    @Override
    public Investor getAssociatedInvestor() throws MissingTransactionInformationException {
        try {
            return getInvestorService().getInvestorByBitcoinAddress(getReceivingAddress());
        } catch (MissingTransactionInformationException e) {
            throw e;
        } catch (Exception e) {
            throw new MissingTransactionInformationException("Couldn't fetch associated investor.", e);
        }
    }

    @Override
    public Long getBlockHeight() throws MissingTransactionInformationException {
        if (this.blockHeight == null) {
            try {
                this.blockHeight = bitcoinjTxOutput.getParentTransaction()
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
            } catch (Exception e) {
                throw new MissingTransactionInformationException("Couldn't fetch block height.", e);
            }
        }
        return this.blockHeight;
    }

    @Override
    public CurrencyType getCurrencyType() {
        return CurrencyType.BTC;
    }

    /*
     * Retrieve the timestamp from the first block that the transaction was seen in.
     */
    @Override
    public Date getBlockTime() throws MissingTransactionInformationException {
        if (this.blockTime == null) {
            try {
                this.blockTime = bitcoinjTxOutput.getParentTransaction()
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
            } catch (Exception e) {
                throw new MissingTransactionInformationException("Couldn't fetch block time.", e);
            }
        }
        return this.blockTime;
    }

    @Override
    public String getTransactionUrl() throws MissingTransactionInformationException {
        try {
            return BLOCKCHAIN_INFO_LINK + bitcoinjTxOutput.getParentTransaction().getHashAsString();
        } catch (Exception e) {
            throw new MissingTransactionInformationException("Couldn't fetch transaction Id.", e);
        }
    }

    /**
     * @return the bitcoinj transaction object.
     */
    public Transaction getBitcoinjTransaction() {
        return bitcoinjTxOutput.getParentTransaction();
    }
}
