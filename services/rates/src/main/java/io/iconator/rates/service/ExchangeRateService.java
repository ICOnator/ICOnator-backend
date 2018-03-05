package io.iconator.rates.service;

import com.github.rholder.retry.Retryer;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.ExchangeType;
import io.iconator.commons.model.db.ExchangeAggregateRate;
import io.iconator.commons.model.db.ExchangeCurrencyRate;
import io.iconator.commons.model.db.ExchangeEntryRate;
import io.iconator.commons.sql.dao.ExchangeAggregateRateRepository;
import io.iconator.rates.client.blockchaininfo.BlockchainInfoClient;
import io.iconator.rates.client.etherscan.EtherScanClient;
import io.iconator.rates.config.RatesAppConfig;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Service
public class ExchangeRateService {

    private final static Logger LOG = LoggerFactory.getLogger(ExchangeRateService.class);

    @Autowired
    private EtherScanClient etherScanClient;

    @Autowired
    private BlockchainInfoClient blockchainInfoClient;

    @Autowired
    private ExchangeAggregateRateRepository exchangeAggregateRateRepository;

    @Autowired
    private RatesAppConfig ratesAppConfig;

    @Autowired
    @Qualifier("bitfinexMarketDataService")
    private MarketDataService bitfinexMarketDataService;

    @Autowired
    @Qualifier("krakenMarketDataService")
    private MarketDataService krakenMarketDataService;

    @Autowired
    @Qualifier("bitstampMarketDataService")
    private MarketDataService bitstampMarketDataService;

    @Autowired
    private Retryer retryer;

    public Optional<BigDecimal> getRate(ExchangeType exchangeType, CurrencyPair currencyPair) {
        Ticker ticker = null;
        try {
            ticker = (Ticker) retryer.call(() -> {
                Ticker t = null;
                switch (exchangeType) {
                    case BITSTAMP:
                        t = bitstampMarketDataService.getTicker(currencyPair);
                    case KRAKEN:
                        t = krakenMarketDataService.getTicker(currencyPair);
                    case BITFINEX:
                        t = bitfinexMarketDataService.getTicker(currencyPair);
                    default:
                        t = krakenMarketDataService.getTicker(currencyPair);

                }
                return t;
            });
        } catch (Exception e) {
            LOG.error(String.format("Could not fetch %s from %s", currencyPair, exchangeType), e);
        }
        return ticker != null ? ofNullable(ticker.getLast()) : Optional.empty();
    }

    @Transactional
    public void fetchRates() {
        LOG.info("Fetching rates...");

        Long blockNrETH = null;
        try {
            blockNrETH = etherScanClient.getCurrentBlockNr();
        } catch (Throwable t) {
            LOG.error("Could not fetch current ETH block number.", t);
        }

        Long blockNrBTC = null;
        try {
            blockNrBTC = blockchainInfoClient.getCurrentBlockNr();
        } catch (Throwable t) {
            LOG.error("Could not fetch current BTC block number.", t);
        }


        List<CurrencyType> enabledCurrencies = ratesAppConfig.getEnabledCurrencies();
        List<ExchangeType> enabledExchanges = ratesAppConfig.getEnabledExchanges();

        ExchangeAggregateRate exchangeAggregateRate = new ExchangeAggregateRate(new Date(), blockNrETH, blockNrBTC);
        enabledExchanges.stream().forEach((enabledExchange) -> {
            ExchangeEntryRate exchangeEntryRate = new ExchangeEntryRate(new Date(), enabledExchange);
            enabledCurrencies.stream().forEach((enabledCurrency) -> {
                Optional<BigDecimal> oRate = getRate(enabledExchange,
                        new CurrencyPair(Currency.getInstance(enabledCurrency.toString()), Currency.USD));
                oRate.ifPresent((rate) -> {
                    ExchangeCurrencyRate exchangeCurrencyRate = new ExchangeCurrencyRate(enabledCurrency, rate);
                    exchangeEntryRate.addCurrencyRate(exchangeCurrencyRate);
                });
            });
            exchangeAggregateRate.addExchangeEntry(exchangeEntryRate);
        });

        exchangeAggregateRateRepository.save(exchangeAggregateRate);
    }

}
