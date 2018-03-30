package io.iconator.monitor;

import io.iconator.commons.amqp.model.FundsReceivedEmailMessage;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.Investor;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.commons.sql.dao.PaymentLogRepository;
import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.monitor.service.FxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.springframework.dao.DataIntegrityViolationException;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.Block;
import org.web3j.utils.Convert;
import org.web3j.utils.Convert.Unit;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.iconator.commons.amqp.model.utils.MessageDTOHelper.build;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Optional.ofNullable;

public class EthereumMonitor extends BaseMonitor {

    private final static Logger LOG = LoggerFactory.getLogger(EthereumMonitor.class);

    private final Web3j web3j;
    private final FxService fxService;
    private final InvestorRepository investorRepository;
    private final PaymentLogRepository paymentLogRepository;
    private boolean started = false;
    private Map<String, String> monitoredAddresses = new HashMap<>(); // public key -> address

    private ICOnatorMessageService messageService;

    public EthereumMonitor(FxService fxService,
                           Web3j web3j,
                           InvestorRepository investorRepository,
                           PaymentLogRepository paymentLogRepository,
                           SaleTierRepository saleTierRepository,
                           ICOnatorMessageService messageService) {
        super(saleTierRepository);
        this.fxService = fxService;
        this.web3j = web3j;
        this.investorRepository = investorRepository;
        this.paymentLogRepository = paymentLogRepository;
        this.messageService = messageService;
    }

    private void fundsReceived(String hash, String address, BigInteger wei, Long blockHeight, Long timestamp) {
        LOG.debug("Detected funds received: wei {}, fromAddress {}, transaction hash {}, blockHeight {}.",
                wei, address, hash, blockHeight);
        // Get exchange rate
        BigDecimal USDperETH;
        try {
            USDperETH = fxService.getUSDperETH(blockHeight);
            LOG.debug("FX Service USDperETH {}, hash {}, address {}", USDperETH.toPlainString(), hash, address);
        } catch (Exception e) {
            LOG.error("Could not fetch exchange rate for ether block {}. {} {}",
                    blockHeight, e.getMessage(), e.getCause());
            return;
        }
        BigDecimal ethers = Convert.fromWei(new BigDecimal(wei), Unit.ETHER);
        BigDecimal usdReceived = ethers.multiply(USDperETH);
        BigDecimal amountTokens = BigDecimal.ZERO;

        // Fetch email
        String publicKey = monitoredAddresses.get(address);
        Optional<Investor> oInvestor = investorRepository.findOptionalByPayInBitcoinPublicKey(publicKey);
        String email = oInvestor.map((investor) -> investor.getEmail()).orElseGet(() -> {
            LOG.error("Could not fetch email address for public key {} / address {}.", monitoredAddresses.get(address), address);
            return null;
        });

        try {
            LOG.debug("USD {} to be converted to tokens, hash {}", usdReceived.toPlainString(), hash);
            Date dateTimestamp = timestamp != null ? new Date(Instant.ofEpochMilli(timestamp).getEpochSecond()) : null;

//            ConversionResult result = calcTokensAndUpdateTiers(usdReceived, dateTimestamp);
//            if (result.hasOverflow()) {
//                // TODO: 2018-03-30 Claude:
//                // Handle overflow of payment which could not be converted into tokens due to last tier being full.
//            }
//            amountTokens = result.getTokens();

            PaymentLog paymentLog = new PaymentLog(hash, new Date(), dateTimestamp,
                    CurrencyType.ETH, new BigDecimal(wei), USDperETH, usdReceived, email, amountTokens);
            Optional<PaymentLog> oSavedPaymentLog = ofNullable(paymentLogRepository.save(paymentLog));
            oSavedPaymentLog.ifPresent(p -> {
                final String etherscanLink = "https://etherscan.io/tx/" + hash;

                FundsReceivedEmailMessage fundsReceivedEmailMessage = new FundsReceivedEmailMessage(
                        build(oInvestor.get()),
                        new BigDecimal(wei),
                        CurrencyType.ETH,
                        etherscanLink,
                        // TODO: 05.03.18 Guil:
                        // calculate the tokens amount!
                        null
                );

                messageService.send(fundsReceivedEmailMessage);

                LOG.info("Pay-in received: {} ETH / {} USD / {} FX / {} / Time: {} / Address: {} / Tokens Amount {}",
                        ethers,
                        p.getPaymentAmount(),
                        p.getFxRate(),
                        p.getEmail(),
                        p.getCreateDate(),
                        address,
                        p.getTokenAmount());

            });

        } catch (DataIntegrityViolationException e) {
            LOG.info("Pay-in already exists on the database: {} ETH / {} USD / {} FX / {} / Block: {} / Address: {}",
                    ethers,
                    usdReceived,
                    USDperETH,
                    email,
                    blockHeight,
                    address);
        } catch (Exception e) {
            LOG.error("Could not save pay-in: {} / {} USD / {} FX / {} / Block: {} / Address: {}",
                    ethers,
                    usdReceived,
                    USDperETH,
                    email,
                    blockHeight,
                    address);
        }

    }

    /**
     * Add a public key we want to monitor
     *
     * @param publicKey Ethereum public key as hex string
     */
    public synchronized void addMonitoredEtherPublicKey(String publicKey) {
        String addressString = Hex.toHexString(org.ethereum.crypto.ECKey.fromPublicOnly(Hex.decode(publicKey)).getAddress());
        if (!addressString.startsWith("0x"))
            addressString = "0x" + addressString;
        LOG.info("Add monitored Ethereum Address: {}", addressString);
        monitoredAddresses.put(addressString.toLowerCase(), publicKey);
    }

    public void start(Long startBlock) throws IOException {
        if (!started) {
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

            started = true;

            web3j.catchUpToLatestAndSubscribeToNewBlocksObservable(
                    new DefaultBlockParameterNumber(startBlock),
                    true
            ).subscribe(ethBlock -> {


            });

            web3j.catchUpToLatestAndSubscribeToNewTransactionsObservable(
                    new DefaultBlockParameterNumber(startBlock))
                    .subscribe(tx -> {
                        if (monitoredAddresses.get(tx.getTo()) != null) {
                            // Money was paid to a monitored address
                            BigInteger timestamp = null;
                            try {
                                Request<?, EthBlock> ethBlockRequest = web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(tx.getBlockNumber()), false);
                                EthBlock blockRequest = ethBlockRequest.send();
                                timestamp = blockRequest.getBlock().getTimestamp();
                            } catch (Exception e) {
                                LOG.error("Not able to fetch the block timestamp.");
                            }

                            try {
                                fundsReceived(tx.getHash(), tx.getTo(), tx.getValue(), tx.getBlockNumber().longValue(), timestamp.longValue());
                            } catch (Throwable e) {
                                LOG.error("Error in fundsReceived:", e);
                            }
                        }

                        if (monitoredAddresses.get(tx.getFrom().toLowerCase()) != null) {
                            // This should normally not happen as it means funds are stolen!
                            LOG.error("WARN: Removed: {} wei from pay-in address", tx.getValue().toString());
                        }
                    }, throwable -> {
                        LOG.error("Error during scanning of txs: ", throwable);
                    });
        } else {
            LOG.warn("io.iconator.monitor.EthereumMonitor is already started");
        }
    }

}
