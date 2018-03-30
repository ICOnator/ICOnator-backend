package io.iconator.monitor;

import io.iconator.commons.amqp.model.FundsReceivedEmailMessage;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.bitcoin.BitcoinUtils;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.commons.sql.dao.PaymentLogRepository;
import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.monitor.service.FxService;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionConfidence.Listener;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.Wallet;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.iconator.commons.amqp.model.utils.MessageDTOHelper.build;
import static java.util.Optional.ofNullable;
import static org.bitcoinj.core.TransactionConfidence.ConfidenceType.BUILDING;
import static org.bitcoinj.core.TransactionConfidence.ConfidenceType.DEAD;
import static org.bitcoinj.core.TransactionConfidence.ConfidenceType.IN_CONFLICT;

public class BitcoinMonitor extends BaseMonitor {

    private final static Logger LOG = LoggerFactory.getLogger(BitcoinMonitor.class);

    private Wallet wallet;
    private final Context bitcoinContext;
    private final PeerGroup peerGroup;
    private final NetworkParameters bitcoinNetworkParameters;
    private final BlockChain bitcoinBlockchain;
    private final SPVBlockStore bitcoinBlockStore;
    private final FxService fxService;
    private final InvestorRepository investorRepository;
    private final PaymentLogRepository paymentLogRepository;
    private Set<TransactionOutput> processedUTXOs = new HashSet<>();
    private Map<String, String> monitoredAddresses = new HashMap<>();

    private ICOnatorMessageService messageService;

    public BitcoinMonitor(FxService fxService,
                          BlockChain bitcoinBlockchain,
                          SPVBlockStore bitcoinBlockStore,
                          Context bitcoinContext,
                          NetworkParameters bitcoinNetworkParameters,
                          PeerGroup peerGroup,
                          InvestorRepository investorRepository,
                          PaymentLogRepository paymentLogRepository,
                          SaleTierRepository saleTierRepository,
                          ICOnatorMessageService messageService) throws Exception {
        super(saleTierRepository);
        this.fxService = fxService;

        this.bitcoinBlockchain = bitcoinBlockchain;
        this.bitcoinBlockStore = bitcoinBlockStore;
        this.bitcoinContext = bitcoinContext;
        this.bitcoinNetworkParameters = bitcoinNetworkParameters;
        this.peerGroup = peerGroup;
        this.investorRepository = investorRepository;
        this.paymentLogRepository = paymentLogRepository;

        this.messageService = messageService;

        walletSetUp();

        addCoinsReceivedListener();
    }

    private void walletSetUp() {
        this.wallet = new Wallet(this.bitcoinContext);
        this.bitcoinBlockchain.addWallet(wallet);
        peerGroup.addWallet(wallet);
    }

    /**
     * Add a public key we want to monitor
     *
     * @param publicKey Bitcoin public key as hex string
     * @param timestamp Timestamp in seconds when this key was created
     */
    public synchronized void addMonitoredPublicKey(String publicKey, long timestamp) {
        final Address address = ECKey.fromPublicOnly(Hex.decode(publicKey))
                .toAddress(this.bitcoinNetworkParameters);
        final String addressString = address.toBase58();
        LOG.info("Add monitored Bitcoin Address: {}", addressString);
        wallet.addWatchedAddress(address, timestamp);
        monitoredAddresses.put(addressString, publicKey);
    }

    public void start() throws InterruptedException {
        peerGroup.start();

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
        peerGroup.startBlockChainDownload(downloadListener);
        LOG.info("Downloading SPV blockchain...");
        downloadListener.await();
    }

    /**
     * Listens for changes to watched addresses
     */
    private void addCoinsReceivedListener() {
        wallet.addCoinsReceivedEventListener((wallet1, tx, prevBalance, newBalance) -> {
            Context.propagate(this.bitcoinContext);
            // Check outputs
            tx.getOutputs().forEach(utxo -> {
                // If not already processed and this output sends to one of our watched addresses
                if (!processedUTXOs.contains(utxo) && utxo.getScriptPubKey().isSentToAddress()) {
                    Address address = utxo.getAddressFromP2PKHScript(this.bitcoinNetworkParameters);
                    if (wallet1.getWatchedAddresses().contains(address)) {

                        // If the confidence is already BUILDING (1 block or more on best chain)
                        // we have a hit
                        if (BitcoinUtils.isBuilding(tx)) {
                            coinsReceived(utxo);

                            // If pending or unknown we add a confidence changed listener and wait for block inclusion
                        } else if (BitcoinUtils.isPending(tx) || BitcoinUtils.isUnknown(tx)) {
                            LOG.info("Pending: {} satoshi received in {}", utxo.getValue(), tx.getHashAsString());
                            Listener listener = new Listener() {
                                @Override
                                public void onConfidenceChanged(TransactionConfidence confidence, ChangeReason reason) {
                                    if (!processedUTXOs.contains(utxo)) {
                                        if (confidence.getConfidenceType().equals(BUILDING)) {
                                            coinsReceived(utxo);
                                            tx.getConfidence().removeEventListener(this);
                                        } else if (confidence.getConfidenceType().equals(DEAD) || confidence
                                                .getConfidenceType().equals(IN_CONFLICT)) {
                                            tx.getConfidence().removeEventListener(this);
                                        }
                                    }
                                }
                            };
                            tx.getConfidence().addEventListener(listener);
                        }
                    }
                }
            });
        });
    }

    /**
     * We have some funds send to us. This is called live or when catching-up at startup.
     *
     * @param utxo The transaction output we received
     */
    private void coinsReceived(TransactionOutput utxo) {
        long satoshi = utxo.getValue().getValue();
        final String address = utxo.getAddressFromP2PKHScript(this.bitcoinNetworkParameters).toBase58();

        // Retrieve the timestamp from the first block that this transaction was seen in
        long timestamp = utxo.getParentTransaction().getAppearsInHashes().keySet().stream()
                .map((blockHash) -> {
                    try {
                        return this.bitcoinBlockStore.get(blockHash);
                    } catch (BlockStoreException e) {
                        return null; // This can happen if the transaction was seen in a side-chain
                    }
                })
                .filter(Objects::nonNull)
                .map(StoredBlock::getHeader)
                .map(Block::getTime)
                .mapToLong(date -> (date.getTime() / 1000L))
                .min().orElse(0L);
        if (timestamp == 0L) {
            LOG.error("Could not get time for utxo in tx {} with satoshi value {}",
                    address, satoshi);
            return;
        }

        // Calculate USD value
        BigDecimal USDperBTC = null;
        try {
            USDperBTC = fxService.getUSDPerBTC(timestamp);
        } catch (Exception e) {
            LOG.error("Could not fetch exchange rate for utxo in tx {} with satoshi value {}. {} {}",
                    address, satoshi, e.getMessage(), e.getCause());
            return;
        }
        BigDecimal usdReceived = BigDecimal.valueOf(satoshi)
                .multiply(USDperBTC)
                .divide(BigDecimal.valueOf(100_000_000L), BigDecimal.ROUND_DOWN);

        // Fetch email
        String publicKey = monitoredAddresses.get(address);
        Optional<Investor> oInvestor = investorRepository.findOptionalByPayInBitcoinPublicKey(publicKey);
        String email = oInvestor.map((investor) -> investor.getEmail()).orElseGet(() -> {
            LOG.error("Could not fetch email address for public key {} / address {}.", publicKey, address);
            return null;
        });

        final String identifier = utxo.getParentTransaction().getHashAsString() + "_"
                + String.valueOf(utxo.getIndex());
        BigInteger value = new BigInteger(String.valueOf(satoshi));
        Instant blockTime = Instant.ofEpochSecond(timestamp);
        BigDecimal amountTokens = BigDecimal.ZERO;

        try {

            //  ConversionResult result = calcTokensAndUpdateTiers(usdReceived, blockTime);
            //  if (result.hasOverflow()) {
            //      TODO: 2018-03-30 Claude:
            //      Handle overflow of payment which could not be converted into tokens due to last tier being full.
            //  }
            // amountTokens = new BigDecimal(result.getTokens());

            PaymentLog paymentLog = new PaymentLog(identifier, new Date(), new Date(blockTime.toEpochMilli()),
                    CurrencyType.BTC, new BigDecimal(value), USDperBTC, usdReceived, email, amountTokens);
            Optional<PaymentLog> oSavedPaymentLog = ofNullable(paymentLogRepository.save(paymentLog));
            oSavedPaymentLog.ifPresent(p -> {
                final String blockChainInfoLink = "https://blockchain.info/tx/" + utxo.getParentTransaction().getHashAsString();

                FundsReceivedEmailMessage fundsReceivedEmailMessage = new FundsReceivedEmailMessage(
                        build(oInvestor.get()),
                        new BigDecimal(value),
                        CurrencyType.BTC,
                        blockChainInfoLink,
                        // TODO: 05.03.18 Guil:
                        // calculate the tokens amount!
                        null
                );

                messageService.send(fundsReceivedEmailMessage);

                LOG.info("Pay-in received: {} / {} USD / {} FX / {} / Time: {} / Address: {} / Tokens Amount {}",
                        utxo.getValue().toFriendlyString(),
                        p.getPaymentAmount(),
                        p.getFxRate(),
                        p.getEmail(),
                        p.getCreateDate(),
                        address,
                        p.getTokenAmount());
            });

        } catch (DataIntegrityViolationException e) {
            LOG.info("Pay-in already exists on the database: {} / {} USD / {} FX / {} / Time: {] / Address: {}",
                    utxo.getValue().toFriendlyString(),
                    usdReceived,
                    USDperBTC,
                    email,
                    timestamp,
                    address);
        } catch (Exception e) {
            LOG.error("Could not save pay-in: {} / {} USD / {} FX / {} / Time: {] / Address: {}",
                    utxo.getValue().toFriendlyString(),
                    usdReceived,
                    USDperBTC,
                    email,
                    timestamp,
                    address);
        }

        processedUTXOs.add(utxo);
    }

}
