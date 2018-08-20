package io.iconator.monitor;

import io.iconator.commons.amqp.model.BlockNRBitcoinMessage;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.bitcoin.BitcoinUtils;
import io.iconator.commons.db.services.InvestorService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.MonitorService;
import io.iconator.monitor.transaction.BitcoinTransactionAdapter;
import org.bitcoinj.core.*;
import org.bitcoinj.core.listeners.BlocksDownloadedEventListener;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Date;

import static org.bitcoinj.core.TransactionConfidence.ConfidenceType.*;

public class BitcoinMonitor extends BaseMonitor {

    private final static Logger LOG = LoggerFactory.getLogger(BitcoinMonitor.class);

    private Wallet wallet;
    private final Context bitcoinContext;
    private final PeerGroup bitcoinPeerGroup;
    private final NetworkParameters bitcoinNetworkParameters;
    private final BlockChain bitcoinBlockchain;
    private final SPVBlockStore bitcoinBlockStore;

    public BitcoinMonitor(FxService fxService,
                          BlockChain bitcoinBlockchain,
                          SPVBlockStore bitcoinBlockStore,
                          Context bitcoinContext,
                          NetworkParameters bitcoinNetworkParameters,
                          PeerGroup bitcoinPeerGroup,
                          PaymentLogService paymentLogService,
                          MonitorService monitorService,
                          ICOnatorMessageService messageService,
                          InvestorService investorService) {

        super(monitorService, paymentLogService, fxService, messageService, investorService);

        this.bitcoinBlockchain = bitcoinBlockchain;
        this.bitcoinBlockStore = bitcoinBlockStore;
        this.bitcoinContext = bitcoinContext;
        this.bitcoinNetworkParameters = bitcoinNetworkParameters;
        this.bitcoinPeerGroup = bitcoinPeerGroup;

        walletSetUp();

        addCoinsReceivedListener();
    }

    private void walletSetUp() {
        this.wallet = new Wallet(this.bitcoinContext);
        this.bitcoinBlockchain.addWallet(wallet);
        bitcoinPeerGroup.addWallet(wallet);
    }

    /**
     * Add a public key we want to monitor
     *
     * @param addressString Bitcoin address
     * @param timestamp     Timestamp in seconds when this key was created
     */
    public synchronized void addMonitoredAddress(String addressString, long timestamp) {
        final Address address = Address.fromBase58(bitcoinNetworkParameters, addressString);
        LOG.info("Add monitored Bitcoin Address: {}", addressString);
        wallet.addWatchedAddress(address, timestamp);
    }

    public void start() throws InterruptedException {
        bitcoinPeerGroup.start();

        // Download block chain (blocking)
        final DownloadProgressTracker downloadListener = new DownloadProgressTracker() {
            @Override
            protected void doneDownload() {
                LOG.info("Download done, now sending block numbers ");
                final int startBlockHeighth = bitcoinBlockchain.getBestChainHeight();
                messageService.send(new BlockNRBitcoinMessage(Long.valueOf(startBlockHeighth), new Date().getTime()));
                bitcoinPeerGroup.addBlocksDownloadedEventListener(new BlocksDownloadedEventListener() {
                    @Override
                    public void onBlocksDownloaded(Peer peer, Block block, @Nullable FilteredBlock filteredBlock, int blocksLeft) {
                        if (bitcoinBlockchain.getBestChainHeight() > startBlockHeighth) {
                            messageService.send(new BlockNRBitcoinMessage(Long.valueOf(bitcoinBlockchain.getBestChainHeight()), new Date().getTime()));
                        }
                    }
                });
            }

            @Override
            protected void progress(double pct, int blocksSoFar, Date date) {
                LOG.info("Downloading chain: {}%", (int) pct);
            }
        };
        bitcoinPeerGroup.startBlockChainDownload(downloadListener);
        LOG.info("Downloading SPV blockchain...");
        //TB: needed to disable this, otherwise it does not start within the 60s of HEROKU
        //downloadListener.await();
    }

    /**
     * Adds a listener to the wallet which processes payments to monitored addresses.
     */
    private void addCoinsReceivedListener() {
        wallet.addCoinsReceivedEventListener((wallet_, bitcoinjTx, prevBalance, newBalance) -> {
            Context.propagate(this.bitcoinContext);
            bitcoinjTx.getOutputs().stream()
                    .filter(utxo -> utxo.getScriptPubKey().isSentToAddress())
                    .map(utxo -> new BitcoinTransactionAdapter(
                            utxo, bitcoinNetworkParameters, bitcoinBlockStore, investorService))
                    .forEach(tx -> {
                        try {
                            if (BitcoinUtils.isBuilding(tx.getBitcoinjTransaction())) {
                                processBuildingTransaction(tx);
                            // TODO [claude] the transaction confidence check needs to be incorporated
                            // into the BaseMonitor code. Otherwise we will track all transactions
                            // which do not yet have high confidence.
                            } else if (BitcoinUtils.isPending(tx.getBitcoinjTransaction())
                                    || BitcoinUtils.isUnknown(tx.getBitcoinjTransaction())) {
                                LOG.info("Confirmation for transaction {} is pending or unknown", tx.getTransactionId());
                                tx.getBitcoinjTransaction().getConfidence()
                                        .addEventListener(new TransactionConfidenceListener(tx));
                            }
                        } catch (Throwable t) {
                            LOG.error("Error while processing transaction.", t);
                        }
                    });
        });
    }

    @Override
    protected boolean isAddressMonitored(String receivingAddress) {
        return wallet.getWatchedAddresses().stream().anyMatch(
                a -> a.toBase58().contentEquals(receivingAddress));
    }

    private class TransactionConfidenceListener implements TransactionConfidence.Listener {

        private BitcoinTransactionAdapter tx;

        TransactionConfidenceListener(BitcoinTransactionAdapter transactionAdapter) {
            this.tx = transactionAdapter;
        }

        @Override
        public void onConfidenceChanged(TransactionConfidence confidence, ChangeReason reason) {
            if (confidence.getConfidenceType().equals(BUILDING)) {
                processBuildingTransaction(tx);
                tx.getBitcoinjTransaction().getConfidence().removeEventListener(this);
            } else if (confidence.getConfidenceType().equals(DEAD)
                    || confidence.getConfidenceType().equals(IN_CONFLICT)) {
                tx.getBitcoinjTransaction().getConfidence().removeEventListener(this);
            }
        }
    }
}
