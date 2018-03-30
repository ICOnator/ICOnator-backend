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

    @Value("${io.iconator.services.rates.blockr.url}")
    private String blockrUrl;

    @Value("${io.iconator.services.rates.currencies.enabled}")
    private CurrencyType[] enabledCurrencies;

    @Value("${io.iconator.services.rates.exchanges.enabled}")
    private ExchangeType[] enabledExchanges;

    @Value("${io.iconator.services.rates.retry.attempts.max}")
    private Integer maxAttempts;

    @Value("${io.iconator.services.rates.retry.wait-between-attemps.min}")
    private Long minTimeWait;

    @Value("${io.iconator.services.rates.retry.wait-between-attemps.max}")
    private Long maxTimeWait;

    public String getUserAgent() {
        return userAgent;
    }

    public String getEtherScanApiToken() {
        return etherScanApiToken;
    }

    public String getEtherScanUrl() {
        return etherScanUrl;
    }

    public String getBlockrUrl() {
        return blockrUrl;
    }

    public List<CurrencyType> getEnabledCurrencies() {
        return Arrays.asList(enabledCurrencies);
    }

    public List<ExchangeType> getEnabledExchanges() {
        return Arrays.asList(enabledExchanges);
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
}
