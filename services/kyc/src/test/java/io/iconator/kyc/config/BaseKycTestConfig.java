package io.iconator.kyc.config;

import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.commons.sql.dao.KycInfoRepository;
import io.iconator.kyc.service.InvestorService;
import io.iconator.kyc.service.KycInfoService;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories("io.iconator.commons.sql.dao")
@EntityScan(basePackages = {"io.iconator.commons.model"})
public class BaseKycTestConfig {

    @Bean
    public KycInfoService kycInfoService(KycInfoRepository kycInfoRepository) {
        return new KycInfoService(kycInfoRepository);
    }

    @Bean
    public InvestorService investorService(InvestorRepository investorRepository) {
        return new InvestorService(investorRepository);
    }
}

