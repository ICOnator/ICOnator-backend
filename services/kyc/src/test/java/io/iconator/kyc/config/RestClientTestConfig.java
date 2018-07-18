package io.iconator.kyc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.kyc.service.idnow.IdNowIdentificationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientTestConfig {

    @Bean
    public IdNowIdentificationService idNowIdentificationFetcher() {
        return new IdNowIdentificationService();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
