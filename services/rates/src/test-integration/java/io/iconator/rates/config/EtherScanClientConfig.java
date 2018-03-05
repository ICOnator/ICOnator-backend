package io.iconator.rates.config;

import io.iconator.rates.client.etherscan.EtherScanClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EtherScanClientConfig {

    @Bean
    public EtherScanClient etherScanClient() {
        return new EtherScanClient();
    }

}
