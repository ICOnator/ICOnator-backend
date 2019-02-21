package io.iconator.rates.service;

import com.github.rholder.retry.Retryer;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.ExchangeType;
import io.iconator.commons.model.db.ExchangeAggregateCurrencyRate;
import io.iconator.commons.model.db.ExchangeAggregateRate;
import io.iconator.commons.model.db.ExchangeCurrencyRate;
import io.iconator.commons.model.db.ExchangeEntryRate;
import io.iconator.commons.sql.dao.ExchangeAggregateRateRepository;
import io.iconator.rates.config.RatesAppConfigHolder;
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

/**
 * Holds the logic for fetching exchange rates from multiple exchanges.
 */
@Service
public class ExchangeRateService {

    private final static Logger LOG = LoggerFactory.getLogger(ExchangeRateService.class);

    @Autowired
    private BlockNrProviderService blockNrProviderService;

    @Autowired
    private ExchangeAggregateRateRepository exchangeAggregateRateRepository;

    @Autowired
    private AggregationService aggregationService;

    @Autowired
    private RatesAppConfigHolder ratesAppConfigHolder;

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

    /**
     * Feches the exchange rate for the given currency pair from the given exchange.
     * @param exchangeType The exchange service to fetch the rate from.
     * @param currencyPair The currency pair for which to fetch the rate.
     * @return an optional object containing the rate or an empty one if the rate could not be
     * retrieved.
     */
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

    /**
     * Fetches and stores exchange rates from each enabled exchange service
     * ({@link RatesAppConfigHolder#enabledExchanges}), for each enabled cryptocurrency
     * ({@link RatesAppConfigHolder#enabledCryptoCurrencies}). Also calculates and stores the
     * aggregated exchange rate of each enabled cryptocurrency over all the enabled exchanges.
     */
    @Transactional
    public void fetchRates() {
        LOG.info("Fetching rates...");

        Long blockNrETH = blockNrProviderService.getCurrentBlockNrEthereum();
        if (blockNrETH == null) {
            LOG.error("Could not fetch current ETH block number.");
            return;
        }

        Long blockNrBTC = blockNrProviderService.getCurrentBlockNrBitcoin();
        if (blockNrBTC == null) {
            LOG.error("Could not fetch current BTC block number.");
            return;
        }

        List<CurrencyType> enabledCryptoCurrencies = ratesAppConfigHolder.getEnabledCryptoCurrencies();
        List<ExchangeType> enabledExchanges = ratesAppConfigHolder.getEnabledExchanges();
        CurrencyType baseFiatCurrency = ratesAppConfigHolder.getBaseFiatCurrency();

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
