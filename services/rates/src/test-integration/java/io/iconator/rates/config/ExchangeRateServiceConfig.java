package io.iconator.rates.config;

import io.iconator.rates.service.ExchangeRateService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExchangeRateServiceConfig {

    @Bean
    public ExchangeRateService exchangeRateService() {
        return new ExchangeRateService();
    }

}
