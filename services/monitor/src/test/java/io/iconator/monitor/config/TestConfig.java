package io.iconator.monitor.config;

import io.iconator.commons.sql.dao.EligibleForRefundRepository;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.commons.sql.dao.PaymentLogRepository;
import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.monitor.BaseMonitor;
import io.iconator.monitor.service.FxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories("io.iconator.commons.sql.dao")
@EntityScan(basePackages = {"io.iconator.commons.model"})
public class TestConfig {

    @Bean
    public FxService fxService() {
        return new FxService();
    }

    @Bean
    public BaseMonitor baseMonitor(SaleTierRepository saleTierRepository,
                                   InvestorRepository investorRepository,
                                   PaymentLogRepository paymentLogRepository,
                                   EligibleForRefundRepository eligibleForRefundRepository,
                                   FxService fxService) {

        return new BaseMonitor(saleTierRepository, investorRepository, paymentLogRepository,
                eligibleForRefundRepository, fxService);
    }

    @Bean
    public MonitorAppConfig monitorAppConfig() {
        return new MonitorAppConfig();
    }
}

