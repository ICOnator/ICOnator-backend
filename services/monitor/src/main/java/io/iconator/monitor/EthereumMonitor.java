package io.iconator.monitor;

import com.github.rholder.retry.Retryer;
import io.iconator.commons.amqp.model.BlockNREthereumMessage;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.db.services.InvestorService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.monitor.config.MonitorAppConfigHolder;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.MonitorService;
import io.iconator.monitor.transaction.EthereumTransactionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.NetPeerCount;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static java.time.temporal.ChronoUnit.MINUTES;

/**
 * Ethereum-specific implementation of the {@link BaseMonitor}.
 * Uses a simple {@link Set} for storing the minitored payment addresses.
 * Uses another {@link Set} for tracking fully processed transactions that have not yet been
 * confirmed (i.e. are not burried under enough blocks yet).
 */
public class EthereumMonitor extends BaseMonitor {

    private final static Logger LOG = LoggerFactory.getLogger(EthereumMonitor.class);

    private final Web3j web3j;
    private boolean started = false;
    private Set<String> monitoredAddresses = new HashSet<>(); // public key -> address
    private Set<EthereumTransactionAdapter> unconfirmedTransactions = new HashSet<>();

    private ICOnatorMessageService messageService;

    public EthereumMonitor(FxService fxService,
                           PaymentLogService paymentLogService,
                           MonitorService monitorService,
                           ICOnatorMessageService messageService,
                           InvestorService investorService,
                           Web3j web3j,
                           MonitorAppConfigHolder configHolder,
                           Retryer retryer) {

        super(monitorService, paymentLogService, fxService, messageService,
                investorService, configHolder, retryer);

        this.web3j = web3j;
        this.messageService = messageService;
    }

    @Override
    public synchronized void addPaymentAddressesForMonitoring(String addressString, Long addressCreationTimestamp) {
        if (!addressString.startsWith("0x"))
            addressString = "0x" + addressString;
        LOG.info("Add monitored Ethereum Address: {}", addressString);
        monitoredAddresses.add(addressString.toLowerCase());
    }

    @Override
    public void start() throws Exception {

        if (started) {
            LOG.warn("{} is already running.", EthereumMonitor.class.getName());
            return;
        }
        // Check if node is up-to-date
        BigInteger blockNumber = web3j.ethBlockNumber().send().getBlockNumber();
        Block highestBlock = web3j.ethGetBlockByNumber(() -> new DefaultBlockParameterNumber(blockNumber).getValue(), false).send().getBlock();

        Instant latestBlockTime = Instant.ofEpochSecond(highestBlock.getTimestamp().longValue());
        LOG.info("Highest ethereum block number from fullnode: {}. Time: {}", blockNumber, latestBlockTime);
        if (latestBlockTime.isBefore(Instant.now().minus(10, MINUTES))) {
            LOG.warn("Ethereum fullnode does not seem to be up-to-date");
        } else {
            LOG.info("Ethereum fullnode seems to be up-to-date");
        }
        messageService.send(new BlockNREthereumMessage(highestBlock.getNumber().longValue(), new Date().getTime()));

        started = true;

        monitorBlockNumbers(highestBlock.getNumber());
        monitorPendingTransactions();
        monitorBuildingTransactions();
        monitorProcessedTransactions();
    }

    /**
     * Subscribes a listener that receives new arriving blocks and sends a
     * {@link BlockNREthereumMessage} containing the new block number to a message queue.
     * @param highestBlockNumber the block number below which no block numbers will be send. This
     *                           should be the latest block number when callling this method.
     */
    private void monitorBlockNumbers(BigInteger highestBlockNumber) {
        Long startBlock = configHolder.getEthereumNodeStartBlock();
        web3j.catchUpToLatestAndSubscribeToNewBlocksObservable(
                new DefaultBlockParameterNumber(startBlock), false)
                .subscribe(block -> {
                    if (block.getBlock().getNumber().compareTo(highestBlockNumber) > 0) {
                        messageService.send(
                                new BlockNREthereumMessage(block.getBlock().getNumber().longValue(),
                                        new Date().getTime()));
                    }
                    LOG.info("Processing block number: {}", block.getBlock().getNumber());
                });
    }

    /**
     * Subscribes a listener that reacts to new transactions that are not yet on a block.
     */
    private void monitorPendingTransactions() {
        web3j.pendingTransactionObservable().subscribe(web3jTx -> {
            try {
                if (!monitoredAddresses.contains(web3jTx.getTo())) return;
                processPendingTransactions(
                        new EthereumTransactionAdapter(web3jTx, web3j, investorService));
            } catch (Throwable t) {
                LOG.error("Error while processing transaction.", t);
            }
        }, t -> LOG.error("Error during scanning of pending transactions.", t));
    }

    /**
     * Subscribes a listener that receives all transactions starting at the block given in
     * the property {@link MonitorAppConfigHolder#ethereumNodeStartBlock}. Of course only
     * transactions directed to a monitored address are processed.
     */
    private void monitorBuildingTransactions() {
        Long startBlock = configHolder.getEthereumNodeStartBlock();
        web3j.catchUpToLatestAndSubscribeToNewTransactionsObservable(new DefaultBlockParameterNumber(startBlock))
                .subscribe(web3jTx -> {
                    try {
                        if (!monitoredAddresses.contains(web3jTx.getTo())) return;
                        EthereumTransactionAdapter tx = new EthereumTransactionAdapter(
                                web3jTx, web3j, investorService);
                        processBuildingTransaction(tx);
                        BigInteger currentBlockNr = web3j.ethBlockNumber().send().getBlockNumber();
                        if (isConfirmed(tx, currentBlockNr)) {
                            confirmTransaction(tx);
                        } else {
                            unconfirmedTransactions.add(tx);
                        }
                    } catch (Throwable t) {
                        LOG.error("Error while processing transaction.", t);
                    }
                }, t -> LOG.error("Error during scanning of transactions.", t));
    }

    /**
     * Subscribes a listener that receives new blocks when they are added to the chain. With each
     * block all processed but unconfirmed tarnsactions are checked for their block depth
     * ({@link MonitorAppConfigHolder#ethereumNodeStartBlock}) and are
     * confirmed if they are burried under enough blocks. When they are confirmed, they are also
     * removed from the unconfirmed list.
     */
    private void monitorProcessedTransactions() {
        web3j.blockObservable(false).subscribe(block -> {
            BigInteger currentBlockNr = block.getBlock().getNumber();
            Iterator<EthereumTransactionAdapter> iterator = unconfirmedTransactions.iterator();
            while (iterator.hasNext()) {
                EthereumTransactionAdapter tx = iterator.next();
                if (isConfirmed(tx, currentBlockNr)) {
                    confirmTransaction(tx);
                    iterator.remove();
                }
            }
        });
    }

    private boolean isConfirmed(EthereumTransactionAdapter tx, BigInteger currentBlockNr) {
        BigInteger goalDepth = BigInteger.valueOf(configHolder.getEthereumConfirmationBlockdepth());
        BigInteger depth = currentBlockNr.subtract(tx.getWeb3jTransaction().getBlockNumber());
        return depth.compareTo(goalDepth) >= 0;
    }

    @Override
    protected boolean isAddressMonitored(String receivingAddress) {
        return monitoredAddresses.contains(receivingAddress);
    }

    /**
     * Regularly logs information about the connected Ethereum full node.
     */
    @Scheduled(fixedRate = 60000)
    public void reportEthereumFullNode() {
        try {
            EthBlockNumber ethBlockNumber = web3j.ethBlockNumber().send();
            NetPeerCount netPeerCount = web3j.netPeerCount().send();
            LOG.info("Ethereum Node: netPeerCount={} ethBlockNumber={}",
                    netPeerCount.getQuantity(), ethBlockNumber.getBlockNumber());
        } catch (Exception e) {
            LOG.error("Could not fetch the current ethBlockNumber or netPeerCount. Please, check Ethereum full node connection. Cause: {}", e.getMessage());
        }
    }
}
