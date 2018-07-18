package io.iconator.kyc.config;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.github.rholder.retry.StopStrategies.stopAfterAttempt;
import static com.github.rholder.retry.WaitStrategies.randomWait;

@Configuration
public class KycConfig {

    @Bean("restTemplateIDNow")
    public RestTemplate restTemplate() {
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory());
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
