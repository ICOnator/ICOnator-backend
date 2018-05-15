package io.iconator.monitor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;

@Profile("dev")
public class EthereumTestPaymentService {

    private final static Logger LOG = LoggerFactory.getLogger(EthereumTestPaymentService.class);

    private Web3j web3j;
    private Credentials credentials;

    public EthereumTestPaymentService(Web3j web3j, String walletPassword, String walletPath)
            throws IOException, CipherException {
        this.credentials = WalletUtils.loadCredentials(walletPassword, walletPath);
        this.web3j = web3j;
    }

    public String pay(String paymentToETHAddress, BigDecimal amount) throws Exception {
        LOG.debug("ETH: Sending funds to {}, amount {}.", paymentToETHAddress, amount.toPlainString());
        BigDecimal amountInWei = Convert.toWei(amount, Convert.Unit.ETHER);
        TransactionReceipt transactionReceipt = Transfer.sendFunds(
                web3j, credentials, paymentToETHAddress,
                amountInWei, Convert.Unit.WEI).send();

        return transactionReceipt.getTransactionHash();

    }

}
