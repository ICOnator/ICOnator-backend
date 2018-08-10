package io.iconator.monitor.transaction;

import io.iconator.commons.ethereum.EthereumUnit;
import io.iconator.commons.ethereum.EthereumUnitConverter;
import io.iconator.commons.model.CurrencyType;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

public class EthereumTransaction implements io.iconator.monitor.transaction.Transaction {

    private Transaction ethereumTx;
    private Web3j web3j;

    /**
     * Remembering block time after first time getter is called because it
     * requires a web request which could fail on subsequent calls to the getter.
     */
    private Date blockTime;

    public EthereumTransaction(@NotNull Transaction tx, @NotNull Web3j web3j) {
        this.ethereumTx = tx;
        this.web3j = web3j;
    }

    @Override
    public String getTransactionId() {
        return ethereumTx.getHash();
    }

    @Override
    public BigInteger getTransactionValue() {
        return ethereumTx.getValue();
    }

    @Override
    public BigDecimal getTransactionValueInMainUnit() {
        return EthereumUnitConverter.convert(
                new BigDecimal(getTransactionValue()),
                EthereumUnit.WEI,
                EthereumUnit.ETHER);
    }

    @Override
    public String getReceivingAddress() {
        return ethereumTx.getTo();
    }

    @Override
    public Long getBlockHeight() {
        return ethereumTx.getBlockNumber().longValue();
    }

    @Override
    public CurrencyType getCurrencyType() {
        return CurrencyType.ETH;
    }

    @Override
    public Date getBlockTime() {
        if (blockTime == null) {
            try {
                Request<?, EthBlock> ethBlockRequest = web3j.ethGetBlockByNumber(
                        new DefaultBlockParameterNumber(ethereumTx.getBlockNumber()),
                        false);
                EthBlock blockRequest = ethBlockRequest.send();
                blockTime = new Date(blockRequest.getBlock().getTimestamp().longValue() * 1000);
                return blockTime;
            } catch (IOException e) {
                throw new IllegalStateException();
            }
        } else return blockTime;
    }
}
