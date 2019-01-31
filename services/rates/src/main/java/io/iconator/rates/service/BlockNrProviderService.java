package io.iconator.rates.service;

import io.iconator.commons.model.CurrencyType;
import io.iconator.rates.config.RatesAppConfigHolder;
import io.iconator.rates.consumer.BlockNrBitcoinConsumer;
import io.iconator.rates.consumer.BlockNrEthereumConsumer;
import io.iconator.rates.service.exceptions.CurrencyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;

@Service
public class BlockNrProviderService {

    private static final Logger LOG = LoggerFactory.getLogger(BlockNrProviderService.class);

    @Autowired
    private RatesAppConfigHolder ratesAppConfigHolder;

    @Autowired
    private BlockNrBitcoinConsumer blockNrBitcoinConsumer;

    @Autowired
    private BlockNrEthereumConsumer blockNrEthereumConsumer;

    @Autowired
    private BlockchainInfoService blockchainInfoService;

    @Autowired
    private EtherscanService etherscanService;

    private Long getBlockNumber(CurrencyType currencyType) throws CurrencyNotFoundException {
        switch (currencyType) {
            case BTC:
                return getCurrentBlockNrBitcoin();
            case ETH:
                return getCurrentBlockNrEthereum();
            default:
                throw new CurrencyNotFoundException("Currency not supported to provide the current block number.");
        }
    }

    public Long getCurrentBlockNrBitcoin() {
        Long blockNr = blockNrBitcoinConsumer.getBlockNr();
        Long timestamp = blockNrBitcoinConsumer.getTimestamp();
        Long fallbackIfOlderThan = ratesAppConfigHolder.getBlockNumberBitcoinFallbackToApiIfOlderThan();
        if (blockNr == null || timestamp == null) {
            //fallback is API call to blockchaininfo
            try {
                LOG.warn("No Bitcoin block since startup");
                return blockchainInfoService.getLatestBitcoinHeight();
            } catch (IOException e) {
                LOG.error("Bitcoin block height fallback failed - start, discarding", e);
                return null;
            }
        }
        if (isTimestampOlderThan(timestamp, fallbackIfOlderThan)) {
            //fallback is API call to blockchaininfo
            try {
                LOG.warn("Bitcoin block over two hours old, using fallback");
                return blockchainInfoService.getLatestBitcoinHeight();
            } catch (IOException e) {
                LOG.error("Bitcoin block height fallback failed, discarding", e);
                return null;
            }
        }
        return blockNr;
    }

    public Long getCurrentBlockNrEthereum() {
        Long blockNr = blockNrEthereumConsumer.getBlockNr();
        Long timestamp = blockNrEthereumConsumer.getTimestamp();
        Long fallbackIfOlderThan = ratesAppConfigHolder.getBlockNumberEthereumFallbackToApiIfOlderThan();
        if (blockNr == null || timestamp == null) {
            //fallback is API call to etherscan
            try {
                LOG.warn("No Ethereum block since startup");
                return etherscanService.getLatestEthereumHeight();
            } catch (IOException e) {
                LOG.error("Ethereum block height fallback failed - starting, discarding", e);
                return null;
            }
        }
        if (isTimestampOlderThan(timestamp, fallbackIfOlderThan)) {
            //fallback is API call to etherscan
            try {
                LOG.warn("Ethereum block over 30 min old, using fallback");
                return etherscanService.getLatestEthereumHeight();
            } catch (IOException e) {
                LOG.error("Ethereum block height fallback failed, discarding", e);
                return null;
            }
        }
        return blockNr;
    }

    private boolean isTimestampOlderThan(Long timestampInSeconds, Long olderThanInMillis) {
        return Instant.ofEpochSecond(timestampInSeconds)
                .plusMillis(olderThanInMillis)
                .isBefore(Instant.now());
    }

}
