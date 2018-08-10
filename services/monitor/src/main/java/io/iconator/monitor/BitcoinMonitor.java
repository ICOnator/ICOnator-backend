package io.iconator.monitor;

import io.iconator.commons.amqp.model.BlockNRBitcoinMessage;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.bitcoin.BitcoinUtils;
import io.iconator.commons.db.services.EligibleForRefundService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.TokenAllocationService;
import org.bitcoinj.core.*;
import org.bitcoinj.core.TransactionConfidence.Listener;
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

    private ICOnatorMessageService messageService;

    public BitcoinMonitor(FxService fxService,
                          BlockChain bitcoinBlockchain,
                          SPVBlockStore bitcoinBlockStore,
                          Context bitcoinContext,
                          NetworkParameters bitcoinNetworkParameters,
                          PeerGroup bitcoinPeerGroup,
                          InvestorRepository investorRepository,
                          PaymentLogService paymentLogService,
                          TokenAllocationService tokenAllocationService,
                          EligibleForRefundService eligibleForRefundService,
                          ICOnatorMessageService messageService) {

        super(tokenAllocationService, investorRepository, paymentLogService,
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
                            && !txIsAlreadyProcessed(txoIdentifier)
                            && utxo.getScriptPubKey().isSentToAddress()) {

                        if (BitcoinUtils.isBuilding(tx)) {
                            processTransactionOutput(utxo);
                        } else if (BitcoinUtils.isPending(tx) || BitcoinUtils.isUnknown(tx)) {
                            // If pending or unknown we add a confidence changed listener and wait for block inclusion
                            LOG.info("Pending: {} satoshi received in transaction output {}", utxo.getValue(), txoIdentifier);
                            Listener listener = new Listener() {
                                @Override
                                public void onConfidenceChanged(TransactionConfidence confidence, ChangeReason reason) {
                                    if (!txIsAlreadyProcessed(txoIdentifier)) {
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
//        BigDecimal USDperBTC, usdReceived, coins;
//        try {
//            USDperBTC = fxService.getUSDPerBTC(blockHeight);
//            usdReceived = BitcoinUtils.convertSatoshiToUsd(satoshi, USDperBTC);
//            coins = BitcoinUnitConverter.convert(satoshi, BitcoinUnit.SATOSHI, BitcoinUnit.COIN);
//        } catch (USDBTCFxException e) {
//            LOG.error("Couldn't get USD to BTC exchange rate for transaction {}.", txoIdentifier, e);
//            saveRefundEntry(satoshi, CurrencyType.BTC, txoIdentifier, RefundReason.FX_RATE_MISSING, investor);
//            return;
//        } catch (RuntimeException e) {
//            LOG.error("Failed to fetch payment amount in US dollars for transaction {}.", txoIdentifier, e);
//            saveRefundEntry(satoshi, CurrencyType.BTC, txoIdentifier, RefundReason.CONVERSION_TO_USD_FAILED, investor);
//            return;
//        } catch (BitcoinUnitConversionNotImplementedException e) {
//            LOG.error("Failed to convert satoshi to bitcoin for transaction {}.", txoIdentifier, e);
//            saveRefundEntry(satoshi, CurrencyType.BTC, txoIdentifier, RefundReason.FAILED_CONVERSION_FROM_SATOSHI_TO_COIN, investor);
//            return;
//        }
//
//        LOG.debug("USD {} to be converted to tokens, for transaction {}", usdReceived.toPlainString(), txoIdentifier);
//        PaymentLog paymentLog = new PaymentLog(
//                txoIdentifier,
//                new Date(),
//                timestamp,
//                CurrencyType.BTC,
//                satoshi,
//                USDperBTC,
//                usdReceived,
//                investor.getId(),
//                BigInteger.ZERO);
//        try {
//            trySavingPaymentLog(paymentLog);
//        } catch (TransactionAlreadyProcessedException e) {
//            LOG.info("Couldn't create payment log entry because transaction " +
//                    "was already processed.", txoIdentifier);
//            return;
//        } catch (Throwable t) {
//            LOG.error("Failed creating payment log for transaction {}.", txoIdentifier, t);
//            saveRefundEntry(satoshi, CurrencyType.BTC, txoIdentifier, RefundReason.CREATING_PAYMENTLOG_FAILED, investor);
//            return;
//        }
//
//        TokenAllocationResult result;
//        try {
//            result = allocateTokensWithRetries(usdReceived, timestamp);
//        } catch (Throwable e) {
//            LOG.error("Failed to distribute payment to tiers for transaction {}. " +
//                    "Deleting PaymentLog created for this transaction", txoIdentifier, e);
//            paymentLogService.delete(paymentLog);
//            saveRefundEntry(satoshi, CurrencyType.BTC, txoIdentifier, RefundReason.CONVERSION_TO_TOKENS_FAILED, investor);
//            return;
//        }
//        BigInteger tomics = result.getDistributedTomics();
//        paymentLog.setTomicsAmount(tomics);
//        paymentLog = paymentLogService.save(paymentLog);
//        if (result.hasOverflow()) {
//            BigInteger overflowSatoshi = BitcoinUtils.convertUsdToSatoshi(result.getOverflow(), USDperBTC);
//            saveRefundEntry(overflowSatoshi, CurrencyType.BTC, txoIdentifier, RefundReason.TOKEN_OVERFLOW, investor);
//        }
//
//        final String blockChainInfoLink = "https://blockchain.info/tx/" + utxo.getParentTransaction().getHashAsString();
//
//        messageService.send(new FundsReceivedEmailMessage(
//                build(investor),
//                coins,
//                CurrencyType.BTC,
//                blockChainInfoLink,
//                tokenAllocationService.convertTomicsToTokens(tomics)));
//
//        LOG.info("Pay-in received: {} / {} USD / {} FX / {} / Time: {} / Address: {} / " +
//                        "Tomics Amount {}",
//                utxo.getValue().toFriendlyString(),
//                paymentLog.getPaymentAmount(),
//                paymentLog.getFxRate(),
//                investor.getEmail(),
//                paymentLog.getCreateDate(),
//                receivingAddress,
//                paymentLog.getTomicsAmount());
    }
}
