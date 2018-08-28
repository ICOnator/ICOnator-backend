package io.iconator.monitor;

import com.github.rholder.retry.Retryer;
import io.iconator.commons.amqp.model.BlockNRBitcoinMessage;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.db.services.InvestorService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.monitor.config.MonitorAppConfigHolder;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.MonitorService;
import io.iconator.monitor.transaction.BitcoinTransactionAdapter;
import org.bitcoinj.core.*;
import org.bitcoinj.core.TransactionConfidence.ConfidenceType;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                          InvestorService investorService,
                          MonitorAppConfigHolder configHolder,
                          Retryer retryer) {

        super(monitorService, paymentLogService, fxService, messageService,
                investorService, configHolder, retryer);

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

    @Override
    public synchronized void addPaymentAddressesForMonitoring(String addressString, Long addressCreationTimestamp) {
        final Address address = Address.fromBase58(bitcoinNetworkParameters, addressString);
        LOG.info("Add monitored Bitcoin Address: {}", addressString);
        wallet.addWatchedAddress(address, addressCreationTimestamp / 1000);
    }

    @Override
    protected void start() {
        bitcoinPeerGroup.start();

        final DownloadProgressTracker downloadListener = new DownloadProgressTracker() {
            @Override
            protected void doneDownload() {
                LOG.info("Download done, now sending block numbers.");
                final int startBlockHeight = bitcoinBlockchain.getBestChainHeight();
                messageService.send(new BlockNRBitcoinMessage((long) startBlockHeight, new Date().getTime()));
                bitcoinPeerGroup.addBlocksDownloadedEventListener((peer, block, filteredBlock, blocksLeft) -> {
                    if (bitcoinBlockchain.getBestChainHeight() > startBlockHeight) {
                        messageService.send(new BlockNRBitcoinMessage((long) bitcoinBlockchain.getBestChainHeight(), new Date().getTime()));
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
    }

    /**
     * Adds a listener to the bitcoinj wallet which processes incoming
     * transactions directed to the wallet's addresses.
     * It is assumed that old transactions which are already in blocks are also
     * handed to the listener when starting up the monitor application.
     * @see <a href="https://groups.google.com/d/msg/bitcoinj/bYcUTimAz9w/jwS_7gOsCwAJ">google groups bitcoinj answer</a>
     */
    private void addCoinsReceivedListener() {
        // The provided listener is only called once per transacton, e.g. when
        // the transaction is first seen on the network. It will not be called
        // again for the same transaction when it is added to a block.
        wallet.addCoinsReceivedEventListener((wallet_, bitcoinjTx, prevBalance, newBalance) -> {
            Context.propagate(this.bitcoinContext);
            bitcoinjTx.getOutputs().stream()
                    .filter(utxo -> utxo.getScriptPubKey().isSentToAddress())
                    .map(utxo -> new BitcoinTransactionAdapter(
                            utxo, bitcoinNetworkParameters, bitcoinBlockStore, investorService))
                    .forEach(tx -> {
                        try {
                            processDependingOnStatus(tx);
                        } catch (Throwable t) {
                            LOG.error("Error while processing transaction.", t);
                        }
                    });
        });
    }

    private void processDependingOnStatus(BitcoinTransactionAdapter tx) {
        Transaction bitcoinjTx = tx.getBitcoinjTransaction();
        if (isPending(bitcoinjTx)) {
            processPendingTransactions(tx);
            trackTransactoinForStatusChanges(tx);
        } else if (isBuilding(bitcoinjTx)) {
            processBuildingTransaction(tx);
            trackTransactoinForStatusChanges(tx);
        } else if (isUnknown(bitcoinjTx)) {
            trackTransactoinForStatusChanges(tx);
        }
    }

    private boolean isPending(Transaction tx) {
        return tx.getConfidence().getConfidenceType().equals(ConfidenceType.PENDING);
    }

    private boolean isBuilding(Transaction bitcoinjTx) {
        return bitcoinjTx.getConfidence().getConfidenceType().equals(ConfidenceType.BUILDING);
    }

    private boolean isUnknown(Transaction tx) {
        return tx.getConfidence().getConfidenceType().equals(ConfidenceType.UNKNOWN);
    }

    private void trackTransactoinForStatusChanges(BitcoinTransactionAdapter tx) {
        tx.getBitcoinjTransaction().getConfidence().addEventListener(
                new TransactionConfidence.Listener() {

                    @Override
                    public void onConfidenceChanged(TransactionConfidence confidence, ChangeReason reason) {
                        if (reason.equals(ChangeReason.TYPE)) {
                            if (confidence.getConfidenceType().equals(ConfidenceType.PENDING)) {
                                processPendingTransactions(tx);
                                // Continue tracking the transaction.
                            } else if (confidence.getConfidenceType().equals(ConfidenceType.BUILDING)) {
                                processBuildingTransaction(tx);
                                // Continue tracking the transaction for confirmation.
                            } else if (confidence.getConfidenceType().equals(DEAD)) {
                                // Stop tracking the transaction.
                                tx.getBitcoinjTransaction().getConfidence().removeEventListener(this);
                            }
                        } else if (reason.equals(ChangeReason.DEPTH)) {
                            if (confidence.getDepthInBlocks() >= configHolder.getBitcoinConfirmationBlockdepth()) {
                                confirmTransaction(tx);
                                tx.getBitcoinjTransaction().getConfidence().removeEventListener(this);
                            }
                        }
                    }
                });
    }


    @Override
    protected boolean isAddressMonitored(String receivingAddress) {
        return wallet.getWatchedAddresses().stream().anyMatch(
                a -> a.toBase58().contentEquals(receivingAddress));
    }
}
