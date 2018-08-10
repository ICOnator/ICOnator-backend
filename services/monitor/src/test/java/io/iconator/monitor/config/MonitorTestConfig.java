package io.iconator.monitor.config;

import io.iconator.commons.db.services.EligibleForRefundService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.db.services.SaleTierService;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.monitor.BaseMonitor;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.TokenAllocationService;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories("io.iconator.commons.sql.dao")
@EntityScan(basePackages = {"io.iconator.commons.model"})
public class MonitorTestConfig {

    @Bean
    public FxService fxService() {
        return new FxService();
    }

    @Bean
    public TokenAllocationService tokenConversionService() {
        return new TokenAllocationService();
    }

    @Bean
    public PaymentLogService paymentLogService() {
        return new PaymentLogService();
    }

    @Bean
    public EligibleForRefundService eligibleForRefundService() {
        return new EligibleForRefundService();
    }

        @Bean
    public BaseMonitor baseMonitor(TokenAllocationService tokenAllocationService,
                                   InvestorRepository investorRepository,
                                   PaymentLogService paymentLogService,
                                   EligibleForRefundService eligibleForRefundService,
                                   FxService fxService) {

        return new BaseMonitor(tokenAllocationService, investorRepository, paymentLogService,
                eligibleForRefundService, fxService);
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

