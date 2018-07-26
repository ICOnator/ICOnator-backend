package io.iconator.kyc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import io.iconator.kyc.service.idnow.IdNowIdentificationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

import static com.github.rholder.retry.StopStrategies.stopAfterAttempt;
import static com.github.rholder.retry.WaitStrategies.randomWait;

@Configuration
public class RestClientTestConfig {

    @Bean
    public IdNowIdentificationService idNowIdentificationFetcher() {
        return new IdNowIdentificationService();
    }

    @Bean("restTemplateIDNow")
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public KycConfigHolder kycConfigHolder() {
        return new KycConfigHolder();
    }

    @Bean
    public Retryer retryer(KycConfigHolder kycConfigHolder) {
        return RetryerBuilder.newBuilder()
                .retryIfExceptionOfType(Exception.class)
                .retryIfRuntimeException()
                .withWaitStrategy(randomWait(kycConfigHolder.getMinTimeWait(), TimeUnit.MILLISECONDS, kycConfigHolder.getMaxTimeWait(), TimeUnit.MILLISECONDS))
                .withStopStrategy(stopAfterAttempt(kycConfigHolder.getMaxAttempts()))
                .build();
    }

}
