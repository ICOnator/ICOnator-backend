package io.iconator.monitor.service;

import org.bitcoinj.core.*;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.web3j.crypto.CipherException;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Profile("dev")
public class BitcoinTestPaymentService {

    private final static Logger LOG = LoggerFactory.getLogger(BitcoinTestPaymentService.class);

    private Context bitcoinContext;
    private NetworkParameters bitcoinNetworkParameters;
    private BlockChain bitcoinBlockchain;
    private PeerGroup peerGroup;

    private Wallet bitcoinWallet;

    public BitcoinTestPaymentService(BlockChain bitcoinBlockchain,
                                     Context bitcoinContext,
                                     NetworkParameters bitcoinNetworkParameters,
                                     PeerGroup peerGroup,
                                     String walletPassword, String walletPath)
            throws IOException, CipherException, UnreadableWalletException {

        this.bitcoinBlockchain = bitcoinBlockchain;
        this.bitcoinContext = bitcoinContext;
        this.bitcoinNetworkParameters = bitcoinNetworkParameters;
        this.peerGroup = peerGroup;

        File walletFile = new File(walletPath);
        if (walletFile.exists()) {
            LOG.info("Wallet exists... trying to load from: {}", walletPath);
            this.bitcoinWallet = Wallet.loadFromFile(new File(walletPath));
        } else {
            LOG.info("Wallet does NOT exist... creating one.", walletPath);
            this.bitcoinWallet = new Wallet(this.bitcoinContext);
            this.bitcoinWallet.saveToFile(walletFile);
        }
        LOG.info("Wallet loaded: address={}", this.bitcoinWallet.currentReceiveAddress());
        this.bitcoinWallet.autosaveToFile(walletFile, 500, TimeUnit.MILLISECONDS, null);
        this.bitcoinBlockchain.addWallet(this.bitcoinWallet);
        peerGroup.addWallet(this.bitcoinWallet);
    }

    public String pay(String paymentToBTCAddress, BigDecimal amount)
            throws InterruptedException, ExecutionException, InsufficientMoneyException {

        LOG.debug("BTC: Sending funds to {}, amount {}.", paymentToBTCAddress, amount.toPlainString());

        Address targetAddress = Address.fromBase58(this.bitcoinNetworkParameters, paymentToBTCAddress);
        Wallet.SendResult result = this.bitcoinWallet.sendCoins(peerGroup, targetAddress, Coin.parseCoin(amount.toPlainString()));
        // Wait for the transaction to propagate across the P2P network, indicating acceptance.
        result.broadcastComplete.get();
        return result.tx.getHashAsString();
    }

}
