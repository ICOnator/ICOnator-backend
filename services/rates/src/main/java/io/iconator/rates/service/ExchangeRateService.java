package io.iconator.rates.service;

import com.github.rholder.retry.Retryer;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.ExchangeType;
import io.iconator.commons.model.db.ExchangeAggregateCurrencyRate;
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
    private AggregationService aggregationService;

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
    @Qualifier("gdaxMarketDataService")
    private MarketDataService gdaxMarketDataService;

    @Autowired
    @Qualifier("coinMarketCapMarketDataService")
    private MarketDataService coinMarketCapMarketDataService;

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
                    case GDAX:
                        t = gdaxMarketDataService.getTicker(currencyPair);
                    case COINMARKETCAP:
                        t = coinMarketCapMarketDataService.getTicker(currencyPair);
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

        List<CurrencyType> enabledCryptoCurrencies = ratesAppConfig.getEnabledCryptoCurrencies();
        List<ExchangeType> enabledExchanges = ratesAppConfig.getEnabledExchanges();
        CurrencyType baseFiatCurrency = ratesAppConfig.getBaseFiatCurrency();

        // create an "ExchangeAggregateRate" -- representing an entry with multiple exchanges and
        // an aggregated rate value for each crypto currency
        ExchangeAggregateRate exchangeAggregateRate = new ExchangeAggregateRate(new Date(), blockNrETH, blockNrBTC);

        // for each exchange
        enabledExchanges.stream().forEach((enabledExchange) -> {
            // create an "ExchangeEntryRate" -- representing an exchange
            ExchangeEntryRate exchangeEntryRate = new ExchangeEntryRate(new Date(), enabledExchange);
            // for each crypto currency
            enabledCryptoCurrencies.stream().forEach((enabledCryptoCurrency) -> {
                // get the actual rate, and add as a currency rate to the "ExchangeEntryRate"
                getRateAndAddCurrencyRate(exchangeEntryRate, enabledExchange, enabledCryptoCurrency, baseFiatCurrency);
            });
            // add to the "ExchangeAggregateRate"
            exchangeAggregateRate.addExchangeEntry(exchangeEntryRate);
        });

        // for each enabled crypto currency
        enabledCryptoCurrencies.stream().forEach((enabledCryptoCurrency) -> {
            // get all "ExchangeCurrencyRate" from all exchanges with such crypto currency
            List<ExchangeCurrencyRate> allExchangeCurrencyRates = exchangeAggregateRate.getAllExchangeCurrencyRates(enabledCryptoCurrency);
            // remove outliers and get the mean
            BigDecimal mean = aggregationService.removeOutliersAndGetMean(allExchangeCurrencyRates);
            // create a new aggregate currency
            ExchangeAggregateCurrencyRate aggCurrencyRate = new ExchangeAggregateCurrencyRate(enabledCryptoCurrency, mean);
            // add to the top-level "ExchangeAggregateRate"
            exchangeAggregateRate.addExchangeAggregateCurrencyRate(aggCurrencyRate);
        });

        exchangeAggregateRateRepository.save(exchangeAggregateRate);
    }

    private void getRateAndAddCurrencyRate(ExchangeEntryRate exchangeEntryRate,
                                           ExchangeType exchangeType,
                                           CurrencyType cryptoType,
                                           CurrencyType fiatType) {
        Optional<BigDecimal> oRate = getRate(exchangeType,
                new CurrencyPair(Currency.getInstance(cryptoType.toString()), convertCurrencyTypes(fiatType)));
        oRate.ifPresent((rate) -> {
            ExchangeCurrencyRate exchangeCurrencyRate = new ExchangeCurrencyRate(cryptoType, rate);
            exchangeEntryRate.addCurrencyRate(exchangeCurrencyRate);
        });
    }

    private Currency convertCurrencyTypes(CurrencyType currencyType) {
        return Currency.getInstanceNoCreate(currencyType.name());
    }

}
