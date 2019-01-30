package io.iconator.commons.cryptocompare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Cryptocompare-related bean declarations.
 */
@Configuration
public class CryptoCompareConfig {

    @Bean("restTemplateCryptoCompare")
    public RestTemplate restTemplate() {
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    }

}
