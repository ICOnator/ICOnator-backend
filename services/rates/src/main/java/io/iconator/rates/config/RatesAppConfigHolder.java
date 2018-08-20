package io.iconator.rates.config;

import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.ExchangeType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class RatesAppConfigHolder {

    @Value("${io.iconator.services.rates.bitcoin-net:}")
    private String bitcoinNet;

    @Value("${io.iconator.services.rates.ethereum-net:}")
    private String ethereumNet;

    @Value("${io.iconator.services.rates.ethereum-key:}")
    private String ethereumKey;

    @Value("${io.iconator.services.rates.user-agent}")
    private String userAgent;

    @Value("${io.iconator.services.rates.currencies.fiat.base}")
    private CurrencyType baseFiatCurrency;

    @Value("${io.iconator.services.rates.currencies.crypto.enabled}")
    private CurrencyType[] enabledCryptoCurrencies;

    @Value("${io.iconator.services.rates.exchanges.enabled}")
    private ExchangeType[] enabledExchanges;

    @Value("${io.iconator.services.rates.fetch.historical.enabled}")
    private Boolean historicalEnabled;

    @Value("${io.iconator.services.rates.fetch.current.periodic.enabled}")
    private Boolean currentPeriodicEnabled;

    @Value("${io.iconator.services.rates.fetch.current.periodic.interval}")
    private Integer currentPeriodicInterval;

    @Value("${io.iconator.services.rates.retry.attempts.max}")
    private Integer maxAttempts;

    @Value("${io.iconator.services.rates.retry.wait-between-attemps.min}")
    private Long minTimeWait;

    @Value("${io.iconator.services.rates.retry.wait-between-attemps.max}")
    private Long maxTimeWait;

    @Value("${io.iconator.services.rates.outliers.std-dev.threshold.lower-bound}")
    private Double outlierStdDevThresholdLowerBound;

    @Value("${io.iconator.services.rates.outliers.std-dev.threshold.upper-bound}")
    private Double outlierStdDevThresholdUpperBound;

    @Value("${io.iconator.services.rates.fetch.range-between-threshold.min}")
    private Long fetchRangeBetweenThresholdMillisMin;

    @Value("${io.iconator.services.rates.fetch.range-between-threshold.max}")
    private Long fetchRangeBetweenThresholdMillisMax;

    public String getBitcoinNet() {
        return bitcoinNet;
    }

    public String getEthereumNet() {
        return ethereumNet;
    }

    public String getEthereumKey() {
        return ethereumKey;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public CurrencyType getBaseFiatCurrency() {
        return baseFiatCurrency;
    }

    public List<CurrencyType> getEnabledCryptoCurrencies() {
        return Arrays.asList(enabledCryptoCurrencies);
    }

    public List<ExchangeType> getEnabledExchanges() {
        return Arrays.asList(enabledExchanges);
    }

    public Boolean getHistoricalEnabled() {
        return historicalEnabled;
    }

    public Boolean getCurrentPeriodicEnabled() {
        return currentPeriodicEnabled;
    }

    public Integer getCurrentPeriodicInterval() {
        return currentPeriodicInterval;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public Long getMinTimeWait() {
        return minTimeWait;
    }

    public Long getMaxTimeWait() {
        return maxTimeWait;
    }

    public Double getOutlierStdDevThresholdLowerBound() {
        return outlierStdDevThresholdLowerBound;
    }

    public Double getOutlierStdDevThresholdUpperBound() {
        return outlierStdDevThresholdUpperBound;
    }

    public Long getFetchRangeBetweenThresholdMillisMin() {
        return fetchRangeBetweenThresholdMillisMin;
    }

    public Long getFetchRangeBetweenThresholdMillisMax() {
        return fetchRangeBetweenThresholdMillisMax;
    }
}
