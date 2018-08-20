package io.iconator.rates.service;

import io.iconator.commons.cryptocompare.CryptoCompareClientService;
import io.iconator.commons.cryptocompare.model.CryptoCompareCurrency;
import io.iconator.commons.cryptocompare.model.CryptoCompareResponseDTO;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.ExchangeAggregateRate;
import io.iconator.commons.sql.dao.ExchangeAggregateRateRepository;
import io.iconator.rates.config.RatesAppConfigHolder;
import io.iconator.rates.service.exceptions.CurrencyNotFoundException;
import io.iconator.rates.service.exceptions.RateNotFoundException;
import io.iconator.rates.service.exceptions.USDBTCFxException;
import io.iconator.rates.service.exceptions.USDETHFxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class RatesProviderService {

    private final static Logger LOG = LoggerFactory.getLogger(RatesProviderService.class);

    @Autowired
    private RatesAppConfigHolder ratesAppConfigHolder;

    @Autowired
    private ExchangeAggregateRateRepository exchangeAggregateRateRepository;

    @Autowired
    private CryptoCompareClientService cryptoCompareClientService;

    public BigDecimal getLatestUSDperETH() throws USDETHFxException {
        Optional<ExchangeAggregateRate> exchangeAggregateRate =
                exchangeAggregateRateRepository.findFirstOptionalByOrderByCreationDateDesc();

        return exchangeAggregateRate.flatMap((aggregateRate) -> aggregateRate.getExchangeAggregateCurrencyRates(CurrencyType.ETH))
                .map((aggCurrencyRate) -> aggCurrencyRate.getAggregateExchangeRate())
                .orElseThrow(() -> new USDETHFxException("No FX aggregation found for USD-ETH."));
    }

    public BigDecimal getLatestUSDPerBTC() throws USDBTCFxException {
        Optional<ExchangeAggregateRate> exchangeAggregateRate =
                exchangeAggregateRateRepository.findFirstOptionalByOrderByCreationDateDesc();

        return exchangeAggregateRate.flatMap((aggregateRate) -> aggregateRate.getExchangeAggregateCurrencyRates(CurrencyType.BTC))
                .map((aggCurrencyRate) -> aggCurrencyRate.getAggregateExchangeRate())
                .orElseThrow(() -> new USDBTCFxException("No FX aggregation found for USD-BTC."));
    }

    public BigDecimal getRate(CurrencyType currencyType, Instant timestamp) throws RateNotFoundException {

        // fetch rates between the startDate and endDate
        Instant startRange = calculateRangeStartDate(timestamp);
        Instant endRange = calculateRangeEndDate(timestamp);
        List<ExchangeAggregateRate> rates = exchangeAggregateRateRepository
                .findAllByCreationDateBetweenOrderByCreationDateDesc(Date.from(startRange), Date.from(endRange));

        // get the closest rate to the specified timestamp
        Optional<ExchangeAggregateRate> closestRateToTimestamp = getClosestRateToTimestamp(timestamp, rates);

        return closestRateToTimestamp
                // obtaining the rate from the DB for the specified currency type
                .flatMap((aggregateRate) -> aggregateRate.getExchangeAggregateCurrencyRates(currencyType))
                .map((aggCurrencyRate) -> aggCurrencyRate.getAggregateExchangeRate())
                .map(Optional::of)
                // if it was not found, try to get from the historical provider
                .orElseGet(() -> getRateFromHistoricalProvider(currencyType, timestamp))
                // if not found, then throw an exception
                .orElseThrow(() -> new RateNotFoundException(
                        String.format("Rate for currency %s with timestamp %s was not found either in the DB " +
                                "or fetching from the historical provider API.", currencyType, timestamp.getEpochSecond())));
    }

    protected Instant calculateRangeStartDate(Instant timestamp) {
        if (timestamp == null) {
            return Instant.now();
        }
        Long min = ratesAppConfigHolder.getFetchRangeBetweenThresholdMillisMin();
        return timestamp.minus(min, ChronoUnit.MILLIS);
    }

    protected Instant calculateRangeEndDate(Instant timestamp) {
        if (timestamp == null) {
            return Instant.now();
        }
        Long max = ratesAppConfigHolder.getFetchRangeBetweenThresholdMillisMax();
        return timestamp.plus(max, ChronoUnit.MILLIS);
    }

    protected Optional<ExchangeAggregateRate> getClosestRateToTimestamp(Instant timestamp, List<ExchangeAggregateRate> rates) {
        if (rates == null || rates.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(Collections.min(rates, new Comparator<ExchangeAggregateRate>() {
                public int compare(ExchangeAggregateRate rate1, ExchangeAggregateRate rate2) {
                    long time1 = rate1.getCreationDate() != null ? rate1.getCreationDate().getTime() : 0;
                    long time2 = rate2.getCreationDate() != null ? rate2.getCreationDate().getTime() : 0;
                    long diff1 = Math.abs(time1 - timestamp.toEpochMilli());
                    long diff2 = Math.abs(time2 - timestamp.toEpochMilli());
                    return Long.compare(diff1, diff2);
                }
            }));
        }
    }

    protected Optional<BigDecimal> getRateFromHistoricalProvider(CurrencyType currencyType, Instant timestamp) {
        try {
            // the CryptoCompare API works a bit differently:
            // e.g., the fromCurrency is ETH, and the USD is the toCurrency,
            // which we can read as: 1 ETH = X USD
            CryptoCompareCurrency fromCurrency = convert(currencyType);
            CryptoCompareCurrency toCurrency = convert(ratesAppConfigHolder.getBaseFiatCurrency());
            CryptoCompareResponseDTO response = cryptoCompareClientService.getHistorical(
                    fromCurrency,
                    Arrays.asList(toCurrency),
                    timestamp.getEpochSecond()
            );
            return response.getRateValue(fromCurrency, toCurrency);
        } catch (Exception e) {
            LOG.error("Historical rates not fetched.", e);
        }
        return Optional.empty();
    }

    private CryptoCompareCurrency convert(CurrencyType currencyType) throws CurrencyNotFoundException {
        try {
            return CryptoCompareCurrency.valueOf(currencyType.name());
        } catch (Exception e) {
            throw new CurrencyNotFoundException("Currency not found on CryptoCompareCurrency class, or not supported.", e);
        }
    }

}
