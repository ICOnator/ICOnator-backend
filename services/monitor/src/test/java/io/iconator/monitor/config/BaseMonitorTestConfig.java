package io.iconator.monitor.config;

import io.iconator.commons.db.services.SaleTierService;
import io.iconator.commons.sql.dao.EligibleForRefundRepository;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.commons.sql.dao.PaymentLogRepository;
import io.iconator.monitor.BaseMonitor;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.TokenConversionService;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories("io.iconator.commons.sql.dao")
@EntityScan(basePackages = {"io.iconator.commons.model"})
public class BaseMonitorTestConfig {

    @Bean
    public FxService fxService() {
        return new FxService();
    }

    @Bean
    public TokenConversionService tokenConversionService() {
        return new TokenConversionService();
    }

    @Bean
    public BaseMonitor baseMonitor(TokenConversionService tokenConversionService,
                                   InvestorRepository investorRepository,
                                   PaymentLogRepository paymentLogRepository,
                                   EligibleForRefundRepository eligibleForRefundRepository,
                                   FxService fxService) {

        return new BaseMonitor(tokenConversionService, investorRepository, paymentLogRepository,
                eligibleForRefundRepository, fxService);
    }

    @Bean
    public MonitorAppConfig monitorAppConfig() {
        return new MonitorAppConfig();
    }

    @Bean
    public SaleTierService saleTierService() {
        return new SaleTierService();
    }
}

