package io.iconator.monitor;

import io.iconator.commons.amqp.model.BlockNREthereumMessage;
import io.iconator.commons.amqp.model.FundsReceivedEmailMessage;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.bitcoin.BitcoinUtils;
import io.iconator.commons.db.services.EligibleForRefundService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.ethereum.EthereumUnit;
import io.iconator.commons.ethereum.EthereumUnitConverter;
import io.iconator.commons.ethereum.exception.EthereumUnitConversionNotImplementedException;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.EligibleForRefund.RefundReason;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.TokenConversionService;
import io.iconator.monitor.service.TokenConversionService.TokenDistributionResult;
import io.iconator.monitor.service.exceptions.USDETHFxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.protocol.core.methods.response.Transaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static io.iconator.commons.amqp.model.utils.MessageDTOHelper.build;
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
                           TokenConversionService tokenConversionService,
                           EligibleForRefundService eligibleForRefundService,
                           ICOnatorMessageService messageService,
                           Web3j web3j) {

        super(tokenConversionService, investorRepository, paymentLogService,
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
                        if(block.getBlock().getNumber().compareTo(highestBlock.getNumber()) > 0) {
                            messageService.send(new BlockNREthereumMessage(block.getBlock().getNumber().longValue(), new Date().getTime()));
                        }
                        LOG.info("Processing block number: {}", block.getBlock().getNumber());
                    });

            web3j.catchUpToLatestAndSubscribeToNewTransactionsObservable(
                    new DefaultBlockParameterNumber(startBlock))
                    .subscribe(tx -> {

                        if (monitoredAddresses.contains(tx.getTo())
                                && isTransactionUnprocessed(tx.getHash())) {

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

    private void processTransaction(Transaction tx) {
        final String txIdentifier = tx.getHash();
        final String receivingAddress = tx.getTo();
        final BigInteger wei = tx.getValue();
        final long blockHeight = tx.getBlockNumber().longValue();

        LOG.debug("Detected funds received: wei {}, receiving address {}, transaction hash {}, " +
                "blockHeight {}.", wei, receivingAddress, txIdentifier, blockHeight);

        Optional<Investor> oInvestor = investorRepository.findOptionalByPayInEtherAddressIgnoreCase(receivingAddress);
        if(!oInvestor.isPresent()) {
            LOG.error("Couldn't fetch investor with public address {} for transaction {}.", receivingAddress, txIdentifier);
            eligibleForRefund(wei, CurrencyType.ETH, txIdentifier,
                    RefundReason.NO_INVESTOR_FOUND_FOR_RECEIVING_ADDRESS, null);
            return;
        }
        Investor investor = oInvestor.get();

        Date timestamp;
        try {
            Request<?, EthBlock> ethBlockRequest = web3j.ethGetBlockByNumber(
                    new DefaultBlockParameterNumber(tx.getBlockNumber()),
                    false);
            EthBlock blockRequest = ethBlockRequest.send();
            timestamp = new Date(blockRequest.getBlock().getTimestamp().longValue());
        } catch (Exception e) {
            LOG.error("Failed fetching block timestamp for transaction {}.", txIdentifier);
            eligibleForRefund(wei, CurrencyType.ETH, txIdentifier,
                    RefundReason.MISSING_BLOCK_TIMESTAMP, investor);
            return;
        }
        LOG.debug("Timestamp of transactions block is {}", timestamp);

        BigDecimal USDperETH, usdReceived, ethers;
        try {
            USDperETH = fxService.getUSDperETH(blockHeight);
            LOG.debug("FX Service USDperETH {}, hash {}, address {}", USDperETH.toPlainString(), txIdentifier, receivingAddress);
            ethers = EthereumUnitConverter.convert(new BigDecimal(wei), EthereumUnit.WEI, EthereumUnit.ETHER);
            usdReceived = ethers.multiply(USDperETH);
        } catch (USDETHFxException e) {
            LOG.error("Couldn't get USD to Ether exchange rate for transaction {}.", txIdentifier, e);
            eligibleForRefund(wei, CurrencyType.ETH, txIdentifier, RefundReason.MISSING_FX_RATE, investor);
            return;
        } catch (RuntimeException e) {
            LOG.error("Failed to fetch payment amount in US dollars for transaction {}.", txIdentifier, e);
            eligibleForRefund(wei, CurrencyType.ETH, txIdentifier, RefundReason.FAILED_CONVERSION_TO_USD, investor);
            return;
        } catch (EthereumUnitConversionNotImplementedException e) {
            LOG.error("Failed to convertAndDistributeToTiers wei to ethers for transaction {}.", txIdentifier, e);
            eligibleForRefund(wei, CurrencyType.ETH, txIdentifier, RefundReason.FAILED_CONVERSION_FROM_WEI_TO_ETHER, investor);
            return;
        }

        LOG.debug("USD {} to be converted to tokens, for transaction {}", usdReceived.toPlainString(), txIdentifier);
        PaymentLog paymentLog = new PaymentLog(
                txIdentifier,
                new Date(),
                timestamp,
                CurrencyType.ETH,
                wei,
                USDperETH,
                usdReceived,
                investor.getId(),
                BigInteger.ZERO);
        try {
            paymentLogService.saveTransactionless(paymentLog);
        } catch (Exception e) {
            if (paymentLogService.existsByTxIdentifier(txIdentifier)) {
                LOG.info("Couldn't create payment log entry because an entry already existed for " +
                        "transaction {}. I.e. transaction was already processed.", txIdentifier);
            } else {
                LOG.error("Failed creating payment log for transaction {}.", txIdentifier, e);
                eligibleForRefund(wei, CurrencyType.ETH, txIdentifier, RefundReason.FAILED_CREATING_PAYMENTLOG, investor);
            }
            return;
        }

        TokenDistributionResult result;
        try {
            LOG.debug("Distributing USD {} to Tiers for transaction {}.", usdReceived, txIdentifier);
            result = convertAndDistributeToTiersWithRetries(usdReceived, timestamp);
        } catch (Throwable e) {
            LOG.error("Failed to convertAndDistributeToTiers payment to tokens for transaction {}. " +
                    "Deleting PaymentLog created for this transaction", txIdentifier, e);
            paymentLogService.delete(paymentLog);
            eligibleForRefund(wei, CurrencyType.ETH, txIdentifier, RefundReason.FAILED_CONVERSION_TO_TOKENS, investor);
            return;
        }
        BigInteger tomics = result.getDistributedTomics();
        LOG.debug("USD amount received was converted to {} atomic token units for transaction {}.", tomics, txIdentifier);

        // TODO if no tokens have been converted
        paymentLog.setTomicsAmount(tomics);
        paymentLog = paymentLogService.save(paymentLog);
        if (result.hasOverflow()) {
            BigInteger overflowWei = BitcoinUtils.convertUsdToSatoshi(result.getOverflow(), USDperETH);
            LOG.debug("The payment of {} wei generated a overflow of {} wei, which go into the refund table.", wei, overflowWei);
            eligibleForRefund(overflowWei, CurrencyType.ETH, txIdentifier, RefundReason.FINAL_TIER_OVERFLOW, investor);
        }

        final String etherscanLink = "https://etherscan.io/tx/" + txIdentifier;

        messageService.send(new FundsReceivedEmailMessage(
                build(investor),
                ethers,
                CurrencyType.ETH,
                etherscanLink,
                tokenConversionService.convertTomicsToTokens(tomics)));

        LOG.info("Pay-in received: {} ETH / {} USD / {} FX / {} / Time: {} / Address: {} / " +
                        "Tomics Amount {}",
                ethers,
                paymentLog.getPaymentAmount(),
                paymentLog.getFxRate(),
                investor.getEmail(),
                paymentLog.getCreateDate(),
                receivingAddress,
                paymentLog.getTomicsAmount());
    }
}
