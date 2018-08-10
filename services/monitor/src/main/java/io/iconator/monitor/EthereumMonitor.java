package io.iconator.monitor;

import io.iconator.commons.amqp.model.BlockNREthereumMessage;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.db.services.EligibleForRefundService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.TokenAllocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlock.Block;

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
                           InvestorRepository investorRepository,
                           PaymentLogService paymentLogService,
                           TokenAllocationService tokenAllocationService,
                           EligibleForRefundService eligibleForRefundService,
                           ICOnatorMessageService messageService,
                           Web3j web3j) {

        super(tokenAllocationService, investorRepository, paymentLogService,
                eligibleForRefundService, fxService);

        this.web3j = web3j;
        this.messageService = messageService;
    }

    /**
     * Add a public key we want to monitor
     *
     * @param addressString Ethereum public address as hex string
     */
    public synchronized void addMonitoredEtherAddress(String addressString) {
        if (!addressString.startsWith("0x"))
            addressString = "0x" + addressString;
        LOG.info("Add monitored Ethereum Address: {}", addressString);
        monitoredAddresses.add(addressString.toLowerCase());
    }

    public void start(Long startBlock) throws IOException {
        if (!started) {
            // Check if node is up-to-date
            BigInteger blockNumber = web3j.ethBlockNumber().send().getBlockNumber();
            Block highestBlock = web3j.ethGetBlockByNumber(() -> new DefaultBlockParameterNumber(blockNumber).getValue(), false).send().getBlock();
            messageService.send(new BlockNREthereumMessage(highestBlock.getNumber().longValue(), new Date().getTime()));
            Instant latestBlockTime = Instant.ofEpochSecond(highestBlock.getTimestamp().longValue());
            LOG.info("Highest ethereum block number from fullnode: {}. Time: {}", blockNumber, latestBlockTime);
            if (latestBlockTime.isBefore(Instant.now().minus(10, MINUTES))) {
                LOG.warn("Ethereum fullnode does not seem to be up-to-date");
            } else {
                LOG.info("Ethereum fullnode seems to be up-to-date");
            }

            started = true;

            web3j.catchUpToLatestAndSubscribeToNewBlocksObservable(
                    new DefaultBlockParameterNumber(startBlock), false)
                    .subscribe(block -> {
                        if (block.getBlock().getNumber().compareTo(highestBlock.getNumber()) > 0) {
                            messageService.send(new BlockNREthereumMessage(block.getBlock().getNumber().longValue(), new Date().getTime()));
                        }
                        LOG.info("Processing block number: {}", block.getBlock().getNumber());
                    });

            web3j.catchUpToLatestAndSubscribeToNewTransactionsObservable(
                    new DefaultBlockParameterNumber(startBlock))
                    .subscribe(tx -> {

                        if (monitoredAddresses.contains(tx.getTo())
                                && !txIsAlreadyProcessed(tx.getHash())) {

                            try {
                                processTransaction(tx);
                            } catch (Throwable e) {
                                LOG.error("Error while processing transaction.", e);
                            }
                        }
                    }, throwable -> {
                        LOG.error("Error during scanning of txs: ", throwable);
                    });
        } else {
            LOG.warn("io.iconator.monitor.EthereumMonitor is already started");
        }
    }

//    private void processTransaction(Transaction tx) {
//        BigDecimal USDperETH, usdReceived, ethers;
//        try {
//            USDperETH = fxService.getUSDperETH(blockHeight);
//            LOG.debug("FX Service USDperETH {}, hash {}, address {}", USDperETH.toPlainString(), txIdentifier, receivingAddress);
//            ethers = EthereumUnitConverter.convert(new BigDecimal(wei), EthereumUnit.WEI, EthereumUnit.ETHER);
//            usdReceived = ethers.multiply(USDperETH);
//        } catch (USDETHFxException e) {
//            LOG.error("Couldn't get USD to Ether exchange rate for transaction {}.", txIdentifier, e);
//            saveRefundEntry(wei, CurrencyType.ETH, txIdentifier, RefundReason.FX_RATE_MISSING, investor);
//            return;
//        } catch (RuntimeException e) {
//            LOG.error("Failed to fetch payment amount in US dollars for transaction {}.", txIdentifier, e);
//            saveRefundEntry(wei, CurrencyType.ETH, txIdentifier, RefundReason.CONVERSION_TO_USD_FAILED, investor);
//            return;
//        } catch (EthereumUnitConversionNotImplementedException e) {
//            LOG.error("Failed to convert wei to ethers for transaction {}.", txIdentifier, e);
//            saveRefundEntry(wei, CurrencyType.ETH, txIdentifier, RefundReason.FAILED_CONVERSION_FROM_WEI_TO_ETHER, investor);
//            return;
//        }
//
//        LOG.debug("USD {} to be converted to tokens, for transaction {}", usdReceived.toPlainString(), txIdentifier);
//        PaymentLog paymentLog = new PaymentLog(
//                txIdentifier,
//                new Date(),
//                timestamp,
//                CurrencyType.ETH,
//                wei,
//                USDperETH,
//                usdReceived,
//                investor.getId(),
//                BigInteger.ZERO);
//        try {
//            paymentLogService.saveTransactionless(paymentLog);
//        } catch (Exception e) {
//            if (paymentLogService.existsByTxIdentifier(txIdentifier)) {
//                LOG.info("Couldn't create payment log entry because an entry already existed for " +
//                        "transaction {}. I.e. transaction was already processed.", txIdentifier);
//            } else {
//                LOG.error("Failed creating payment log for transaction {} even though no entry " +
//                        "for that transaction existed.", txIdentifier, e);
//                saveRefundEntry(wei, CurrencyType.ETH, txIdentifier, RefundReason.CREATING_PAYMENTLOG_FAILED, investor);
//            }
//            return;
//        }
//
//        TokenAllocationResult result;
//        try {
//            LOG.debug("Distributing {} USD for transaction {}.", usdReceived, txIdentifier);
//            result = allocateTokensWithRetries(usdReceived, timestamp);
//        } catch (Throwable e) {
//            LOG.error("Failed to distribute payment to tiers for transaction {}. " +
//                    "Deleting PaymentLog created for this transaction", txIdentifier, e);
//            paymentLogService.delete(paymentLog);
//            saveRefundEntry(wei, CurrencyType.ETH, txIdentifier, RefundReason.CONVERSION_TO_TOKENS_FAILED, investor);
//            return;
//        }
//        BigInteger tomics = result.getDistributedTomics();
//        LOG.debug("{} USD were converted to {} atomic token units for transaction {}.", usdReceived,
//                tomics, txIdentifier);
//
//        paymentLog.setTomicsAmount(tomics);
//        paymentLog = paymentLogService.save(paymentLog);
//
//        if (result.hasOverflow()) {
//            BigInteger overflowWei = BitcoinUtils.convertUsdToSatoshi(result.getOverflow(), USDperETH);
//            LOG.debug("The payment of {} wei generated an overflow of {} wei, which go into the refund table.", wei, overflowWei);
//            saveRefundEntry(overflowWei, CurrencyType.ETH, txIdentifier, RefundReason.TOKEN_OVERFLOW, investor);
//        }
//
//        final String etherscanLink = "https://etherscan.io/tx/" + txIdentifier;
//
//        messageService.send(new FundsReceivedEmailMessage(
//                build(investor),
//                ethers,
//                CurrencyType.ETH,
//                etherscanLink,
//                tokenAllocationService.convertTomicsToTokens(tomics)));
//
//        LOG.info("Pay-in received: {} ETH / {} USD / {} FX / {} / Time: {} / Address: {} / " +
//                        "Tomics Amount {}",
//                ethers,
//                paymentLog.getPaymentAmount(),
//                paymentLog.getFxRate(),
//                investor.getEmail(),
//                paymentLog.getCreateDate(),
//                receivingAddress,
//                paymentLog.getTomicsAmount());
    }
}
