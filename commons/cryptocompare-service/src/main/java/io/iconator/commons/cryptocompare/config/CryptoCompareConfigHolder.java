package io.iconator.commons.cryptocompare.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CryptoCompareConfigHolder {

    @Value("${io.iconator.commons.cryptocompare.base-url}")
    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }
}
