package io.iconator.monitor.transaction;

import io.iconator.commons.db.services.InvestorService;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.Investor;
import io.iconator.monitor.transaction.exception.MissingTransactionInformationException;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.Date;
import java.util.NoSuchElementException;

/**
 * Ethereum-sepcific transaction class.
 */
public class EthereumTransactionAdapter extends BaseTransactionAdapter {

    private static final String ETHERSCAN_LINK = "https://etherscan.io/tx/";

    private Transaction web3jTransaction;
    private Web3j web3j;

    /**
     * Remembering block time after first time getter is called because it
     * requires a web request which could fail on subsequent calls to the getter.
     */
    private Date blockTime;

    public EthereumTransactionAdapter(@NotNull Transaction tx,
                                      @NotNull Web3j web3j,
                                      @NotNull InvestorService investorService) {
        super(investorService);
        this.web3jTransaction = tx;
        this.web3j = web3j;
    }

    @Override
    public String getTransactionId() throws MissingTransactionInformationException {
        try {
            String id = web3jTransaction.getHash();
            if (id == null || id.isEmpty()) throw new NoSuchElementException();
            return id;
        } catch (Exception e) {
            throw new MissingTransactionInformationException("Couldn't fetch transaction id.", e);
        }
    }

    @Override
    public BigInteger getTransactionValue() throws MissingTransactionInformationException {
        try {
            BigInteger value = web3jTransaction.getValue();
            if (value == null) throw new NoSuchElementException();
            return value;
        } catch (Exception e) {
            throw new MissingTransactionInformationException("Couldn't fetch transaction value.", e);
        }
    }

    @Override
    public String getReceivingAddress() throws MissingTransactionInformationException {
        try {
            String address = web3jTransaction.getTo();
            if (address == null || address.isEmpty()) throw new NoSuchElementException();
            return address;
        } catch (Exception e) {
            throw new MissingTransactionInformationException("Couldn't fetch receiving address.", e);
        }
    }

    @Override
    public Investor getAssociatedInvestor() throws MissingTransactionInformationException {
        try {
            return getInvestorService().getInvestorByEthereumAddress(getReceivingAddress());
        } catch (MissingTransactionInformationException e) {
            throw e;
        } catch (Exception e) {
            throw new MissingTransactionInformationException("Couldn't fetch associated investor.", e);
        }
    }

    @Override
    public Long getBlockHeight() throws MissingTransactionInformationException {
        try {
            return web3jTransaction.getBlockNumber().longValue();
        } catch (Exception e) {
            throw new MissingTransactionInformationException("Couldn't fetch block height.", e);
        }
    }

    @Override
    public CurrencyType getCurrencyType() {
        return CurrencyType.ETH;
    }

    @Override
    public Date getBlockTime() throws MissingTransactionInformationException {
        if (this.blockTime == null) {
            try {
                Request<?, EthBlock> ethBlockRequest = web3j.ethGetBlockByNumber(
                        new DefaultBlockParameterNumber(web3jTransaction.getBlockNumber()),
                        false);
                EthBlock blockRequest = ethBlockRequest.send();
                this.blockTime = new Date(blockRequest.getBlock().getTimestamp().longValue() * 1000);
            } catch (Exception e) {
                throw new MissingTransactionInformationException("Couldn't fetch block time.", e);
            }
        }
        return this.blockTime;
    }

    @Override
    public String getTransactionUrl() throws MissingTransactionInformationException {
        return ETHERSCAN_LINK + getTransactionId();
    }

    /**
     * @return the web3j transaction object.
     */
    public Transaction getWeb3jTransaction() {
        return web3jTransaction;
    }
}
