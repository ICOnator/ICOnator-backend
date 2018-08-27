package io.iconator.monitor;

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
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import rx.Subscription;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static java.time.temporal.ChronoUnit.MINUTES;

public class EthereumMonitor extends BaseMonitor {

    private final static Logger LOG = LoggerFactory.getLogger(EthereumMonitor.class);

    private final Web3j web3j;
    private boolean started = false;
    private Set<String> monitoredAddresses = new HashSet<>(); // public key -> address

    private ICOnatorMessageService messageService;

    public EthereumMonitor(FxService fxService,
                           PaymentLogService paymentLogService,
                           MonitorService monitorService,
                           ICOnatorMessageService messageService,
                           InvestorService investorService,
                           Web3j web3j,
                           MonitorAppConfigHolder configHolder) {

        super(monitorService, paymentLogService, fxService, messageService,
                investorService, configHolder);

        this.web3j = web3j;
        this.messageService = messageService;
    }

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
        monitorTransactions();
    }

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

    private void monitorTransactions() {
        web3j.pendingTransactionObservable().subscribe(web3jTx -> {
            try {
                if (!monitoredAddresses.contains(web3jTx.getTo())) return;
                processPendingTransactions(
                        new EthereumTransactionAdapter(web3jTx, web3j, investorService));
            } catch (Throwable t) {
                LOG.error("Error while processing transaction.", t);
            }
        }, t -> LOG.error("Error during scanning of pending transactions.", t));

        Long startBlock = configHolder.getEthereumNodeStartBlock();
        web3j.catchUpToLatestAndSubscribeToNewTransactionsObservable(new DefaultBlockParameterNumber(startBlock))
                .subscribe(web3jTx -> {
                    try {
                        if (!monitoredAddresses.contains(web3jTx.getTo())) return;
                        processBuildingTransaction(
                                new EthereumTransactionAdapter(web3jTx, web3j, investorService));
                    } catch (Throwable t) {
                        LOG.error("Error while processing transaction.", t);
                    }
                }, t -> LOG.error("Error during scanning of transactions.", t));

    }

    @Override
    protected boolean isAddressMonitored(String receivingAddress) {
        return monitoredAddresses.contains(receivingAddress);
    }
}
