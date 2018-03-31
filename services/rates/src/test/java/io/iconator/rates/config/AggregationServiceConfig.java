package io.iconator.rates.config;

import io.iconator.rates.service.AggregationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(RatesAppConfig.class)
public class AggregationServiceConfig {

    @Bean
    public AggregationService aggregationService(RatesAppConfig ratesAppConfig) {
        return new AggregationService(ratesAppConfig);
    }

}
