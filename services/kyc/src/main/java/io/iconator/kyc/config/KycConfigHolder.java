package io.iconator.kyc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KycConfigHolder {

    @Value("${io.iconator.services.kyc.client.retry.attempts.max}")
    private Integer maxAttempts;

    @Value("${io.iconator.services.kyc.client.wait-between-attemps.min}")
    private Long minTimeWait;

    @Value("${io.iconator.services.kyc.client.wait-between-attemps.max}")
    private Long maxTimeWait;

    @Value("${io.iconator.services.kyc.idnow.host}")
    private String idNowHost;

    @Value("${io.iconator.services.kyc.idnow.companyId}")
    private String idNowCompanyId;

    @Value("${io.iconator.services.kyc.idnow.apiKey}")
    private String idNowApiKey;

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public Long getMinTimeWait() {
        return minTimeWait;
    }

    public Long getMaxTimeWait() {
        return maxTimeWait;
    }

    public String getIdNowHost() {
        return idNowHost;
    }

    public String getIdNowCompanyId() {
        return idNowCompanyId;
    }

    public String getIdNowApiKey() {
        return idNowApiKey;
    }
}
