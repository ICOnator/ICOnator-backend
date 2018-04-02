package io.iconator.rates.config;

import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.ExchangeType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class RatesAppConfig {

    @Value("${io.iconator.services.rates.user-agent}")
    private String userAgent;

    @Value("${io.iconator.services.rates.etherscan.api-token}")
    private String etherScanApiToken;

    @Value("${io.iconator.services.rates.etherscan.url}")
    private String etherScanUrl;

    @Value("${io.iconator.services.rates.currencies.fiat.base}")
    private CurrencyType baseFiatCurrency;

    @Value("${io.iconator.services.rates.currencies.crypto.enabled}")
    private CurrencyType[] enabledCryptoCurrencies;

    @Value("${io.iconator.services.rates.exchanges.enabled}")
    private ExchangeType[] enabledExchanges;

    @Value("${io.iconator.services.rates.fetch.periodic.enabled}")
    private Boolean periodicEnabled;

    @Value("${io.iconator.services.rates.fetch.periodic.interval}")
    private Integer periodicInterval;

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

    public String getUserAgent() {
        return userAgent;
    }

    public String getEtherScanApiToken() {
        return etherScanApiToken;
    }

    public String getEtherScanUrl() {
        return etherScanUrl;
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

    public Boolean getPeriodicEnabled() {
        return periodicEnabled;
    }

    public Integer getPeriodicInterval() {
        return periodicInterval;
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
}
