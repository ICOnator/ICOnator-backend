package io.iconator.monitor;

import io.iconator.commons.amqp.model.BlockNRBitcoinMessage;
import io.iconator.commons.amqp.model.BlockNREthereumMessage;
import io.iconator.commons.amqp.model.FundsReceivedEmailMessage;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.bitcoin.BitcoinUnit;
import io.iconator.commons.bitcoin.BitcoinUnitConverter;
import io.iconator.commons.bitcoin.BitcoinUtils;
import io.iconator.commons.bitcoin.exception.BitcoinUnitConversionNotImplementedException;
import io.iconator.commons.db.services.EligibleForRefundService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.EligibleForRefund.RefundReason;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.TokenConversionService;
import io.iconator.monitor.service.TokenConversionService.TokenDistributionResult;
import io.iconator.monitor.service.exceptions.USDBTCFxException;
import org.bitcoinj.core.*;
import org.bitcoinj.core.TransactionConfidence.Listener;
import org.bitcoinj.core.listeners.BlocksDownloadedEventListener;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import static io.iconator.commons.amqp.model.utils.MessageDTOHelper.build;
import static org.bitcoinj.core.TransactionConfidence.ConfidenceType.*;

public class BitcoinMonitor extends BaseMonitor {

    private final static Logger LOG = LoggerFactory.getLogger(BitcoinMonitor.class);

    private Wallet wallet;
    private final Context bitcoinContext;
    private final PeerGroup bitcoinPeerGroup;
    private final NetworkParameters bitcoinNetworkParameters;
    private final BlockChain bitcoinBlockchain;
    private final SPVBlockStore bitcoinBlockStore;

    /* Used in addition to the PaymentLog entries because processing of transactions can fail and
     * lead to a refund but must still be marked as processed.
     */
    private Set<String> monitoredAddresses = new HashSet<>();

    private ICOnatorMessageService messageService;

    public BitcoinMonitor(FxService fxService,
                          BlockChain bitcoinBlockchain,
                          SPVBlockStore bitcoinBlockStore,
                          Context bitcoinContext,
                          NetworkParameters bitcoinNetworkParameters,
                          PeerGroup bitcoinPeerGroup,
                          InvestorRepository investorRepository,
                          PaymentLogService paymentLogService,
                          TokenConversionService tokenConversionService,
                          EligibleForRefundService eligibleForRefundService,
                          ICOnatorMessageService messageService) {

        super(tokenConversionService, investorRepository, paymentLogService,
                eligibleForRefundService, fxService);

        this.bitcoinBlockchain = bitcoinBlockchain;
        this.bitcoinBlockStore = bitcoinBlockStore;
        this.bitcoinContext = bitcoinContext;
        this.bitcoinNetworkParameters = bitcoinNetworkParameters;
        this.bitcoinPeerGroup = bitcoinPeerGroup;

        this.messageService = messageService;

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
     * @param timestamp Timestamp in seconds when this key was created
     */
    public synchronized void addMonitoredAddress(String addressString, long timestamp) {
        final Address address = Address.fromBase58(bitcoinNetworkParameters, addressString);
        LOG.info("Add monitored Bitcoin Address: {}", addressString);
        wallet.addWatchedAddress(address, timestamp);
        monitoredAddresses.add(addressString);
    }

    public void start() throws InterruptedException {
        bitcoinPeerGroup.start();

        // Download block chain (blocking)
        final DownloadProgressTracker downloadListener = new DownloadProgressTracker() {
            @Override
            protected void doneDownload() {
                LOG.info("Download done");
            }

            @Override
            protected void progress(double pct, int blocksSoFar, Date date) {
                LOG.info("Downloading chain: {}%", (int) pct);
            }
        };
        bitcoinPeerGroup.startBlockChainDownload(downloadListener);

        final int startBlockHeighth = bitcoinBlockchain.getBestChainHeight();
        messageService.send(new BlockNRBitcoinMessage(new Date().getTime(), Long.valueOf(startBlockHeighth)));
        bitcoinPeerGroup.addBlocksDownloadedEventListener(new BlocksDownloadedEventListener() {
            @Override
            public void onBlocksDownloaded(Peer peer, Block block, @Nullable FilteredBlock filteredBlock, int blocksLeft) {
                if(bitcoinBlockchain.getBestChainHeight() > startBlockHeighth) {
                    messageService.send(new BlockNRBitcoinMessage(new Date().getTime(), Long.valueOf(bitcoinBlockchain.getBestChainHeight())));
                }
            }
        });

        LOG.info("Downloading SPV blockchain...");
        downloadListener.await();
    }

    /**
     * Listens for changes to watched addresses
     */
    private void addCoinsReceivedListener() {
        wallet.addCoinsReceivedEventListener((wallet1, tx, prevBalance, newBalance) -> {
            Context.propagate(this.bitcoinContext);
            tx.getOutputs().forEach(utxo -> {
                try {
                    String txoIdentifier = BitcoinUtils.getTransactionOutputIdentifier(utxo);
                    Address receivingAddress = utxo.getAddressFromP2PKHScript(this.bitcoinNetworkParameters);
                    if (wallet1.getWatchedAddresses().contains(receivingAddress)
                            && isTransactionUnprocessed(txoIdentifier)
                            && utxo.getScriptPubKey().isSentToAddress()) {

                        if (BitcoinUtils.isBuilding(tx)) {
                            // Transaction is coverd by 1 block or more on best chain.
                            processTransactionOutput(utxo);
                        } else if (BitcoinUtils.isPending(tx) || BitcoinUtils.isUnknown(tx)) {
                            // If pending or unknown we add a confidence changed listener and wait for block inclusion
                            LOG.info("Pending: {} satoshi received in transaction output {}", utxo.getValue(), txoIdentifier);
                            Listener listener = new Listener() {
                                @Override
                                public void onConfidenceChanged(TransactionConfidence confidence, ChangeReason reason) {
                                    if (isTransactionUnprocessed(txoIdentifier)) {
                                        if (confidence.getConfidenceType().equals(BUILDING)) {
                                            processTransactionOutput(utxo);
                                            tx.getConfidence().removeEventListener(this);
                                        } else if (confidence.getConfidenceType().equals(DEAD)
                                                || confidence.getConfidenceType().equals(IN_CONFLICT)) {
                                            tx.getConfidence().removeEventListener(this);
                                        }
                                    }
                                }
                            };
                            tx.getConfidence().addEventListener(listener);
                        }
                    }
                } catch (RuntimeException e) {
                    LOG.error("Failed processing transaction output.", e);
                }
            });
        });
    }

    /**
     * We have some funds send to us. This is called live or when catching-up at startup.
     *
     * @param utxo The transaction output we received
     */
    private void processTransactionOutput(TransactionOutput utxo) {
        BigInteger satoshi = BigInteger.valueOf(utxo.getValue().getValue());

        String txoIdentifier;
        try {
            txoIdentifier = BitcoinUtils.getTransactionOutputIdentifier(utxo);
        } catch (RuntimeException e) {
            LOG.error("Failed fetching identifier for transaction output.", e);
            return;
        }

        String receivingAddress;
        try {
            receivingAddress = utxo.getAddressFromP2PKHScript(this.bitcoinNetworkParameters).toBase58();
        } catch (RuntimeException e) {
            LOG.error("Couldn't fetch receiver address for transaction output {}. " +
                    "Can't process transaction without reciever address.", txoIdentifier, e);
            return;
        }

        Investor investor;
        try {
            investor = investorRepository.findOptionalByPayInBitcoinAddress(receivingAddress).get();
        } catch (NoSuchElementException e) {
            LOG.error("Couldn't fetch investor for transaction {}.", txoIdentifier, e);
            eligibleForRefund(satoshi, CurrencyType.BTC, txoIdentifier,
                    RefundReason.NO_INVESTOR_FOUND_FOR_RECEIVING_ADDRESS, null);
            return;
        }

        Date timestamp;
        try {
            timestamp = BitcoinUtils.getTimestampOfTransaction(utxo.getParentTransaction(), bitcoinBlockStore);
        } catch (RuntimeException e) {
            LOG.error("Failed fetching block timestamp for transaction {}.", txoIdentifier);
            eligibleForRefund(satoshi, CurrencyType.BTC, txoIdentifier,
                    RefundReason.MISSING_BLOCK_TIMESTAMP, investor);
            return;
        }

        Long blockHeight;
        try {
            blockHeight = BitcoinUtils.getBlockHeightOfTransaction(utxo.getParentTransaction(), bitcoinBlockStore).longValue();
        } catch (RuntimeException e) {
            LOG.error("Failed fetching block nr for transaction {}.", txoIdentifier);
            eligibleForRefund(satoshi, CurrencyType.BTC, txoIdentifier,
                    RefundReason.MISSING_BLOCK_BTC_NR, investor);
            return;
        }

        BigDecimal USDperBTC, usdReceived, coins;
        try {
            USDperBTC = fxService.getUSDPerBTC(blockHeight);
            usdReceived = BitcoinUtils.convertSatoshiToUsd(satoshi, USDperBTC);
            coins = BitcoinUnitConverter.convert(satoshi, BitcoinUnit.SATOSHI, BitcoinUnit.COIN);
        } catch (USDBTCFxException e) {
            LOG.error("Couldn't get USD to BTC exchange rate for transaction {}.", txoIdentifier, e);
            eligibleForRefund(satoshi, CurrencyType.BTC, txoIdentifier, RefundReason.MISSING_FX_RATE, investor);
            return;
        } catch (RuntimeException e) {
            LOG.error("Failed to fetch payment amount in US dollars for transaction {}.", txoIdentifier, e);
            eligibleForRefund(satoshi, CurrencyType.BTC, txoIdentifier, RefundReason.FAILED_CONVERSION_TO_USD, investor);
            return;
        } catch (BitcoinUnitConversionNotImplementedException e) {
            LOG.error("Failed to convertAndDistributeToTiers satoshi to bitcoin for transaction {}.", txoIdentifier, e);
            eligibleForRefund(satoshi, CurrencyType.BTC, txoIdentifier, RefundReason.FAILED_CONVERSION_FROM_SATOSHI_TO_COIN, investor);
            return;
        }

        LOG.debug("USD {} to be converted to tokens, for transaction {}", usdReceived.toPlainString(), txoIdentifier);
        PaymentLog paymentLog = new PaymentLog(
                txoIdentifier,
                new Date(),
                timestamp,
                CurrencyType.BTC,
                satoshi,
                USDperBTC,
                usdReceived,
                investor.getId(),
                BigInteger.ZERO);
        try {
            paymentLogService.saveTransactionless(paymentLog);
        } catch (Exception e) {
            if (paymentLogService.existsByTxIdentifier(txoIdentifier)) {
                LOG.info("Couldn't create payment log entry because an entry already existed for " +
                        "transaction {}. I.e. transaction was already processed.", txoIdentifier);
            } else {
                LOG.error("Failed creating payment log for transaction {}.", txoIdentifier, e);
                eligibleForRefund(satoshi, CurrencyType.BTC, txoIdentifier, RefundReason.FAILED_CREATING_PAYMENTLOG, investor);
            }
            return;
        }

        TokenDistributionResult result;
        try {
            result = convertAndDistributeToTiersWithRetries(usdReceived, timestamp);
        } catch (Throwable e) {
            LOG.error("Failed to convertAndDistributeToTiers payment to tokens for transaction {}. " +
                    "Deleting PaymentLog created for this transaction", txoIdentifier, e);
            paymentLogService.delete(paymentLog);
            eligibleForRefund(satoshi, CurrencyType.BTC, txoIdentifier, RefundReason.FAILED_CONVERSION_TO_TOKENS, investor);
            return;
        }
        BigInteger tomics = result.getDistributedTomics();
        paymentLog.setTomicsAmount(tomics);
        paymentLog = paymentLogService.save(paymentLog);
        if (result.hasOverflow()) {
            BigInteger overflowSatoshi = BitcoinUtils.convertUsdToSatoshi(result.getOverflow(), USDperBTC);
            eligibleForRefund(overflowSatoshi, CurrencyType.BTC, txoIdentifier, RefundReason.FINAL_TIER_OVERFLOW, investor);
        }

        final String blockChainInfoLink = "https://blockchain.info/tx/" + utxo.getParentTransaction().getHashAsString();

        messageService.send(new FundsReceivedEmailMessage(
                build(investor),
                coins,
                CurrencyType.BTC,
                blockChainInfoLink,
                tokenConversionService.convertTomicsToTokens(tomics)));

        LOG.info("Pay-in received: {} / {} USD / {} FX / {} / Time: {} / Address: {} / " +
                        "Tomics Amount {}",
                utxo.getValue().toFriendlyString(),
                paymentLog.getPaymentAmount(),
                paymentLog.getFxRate(),
                investor.getEmail(),
                paymentLog.getCreateDate(),
                receivingAddress,
                paymentLog.getTomicsAmount());
    }
}
