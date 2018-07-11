package io.iconator.monitor.config;

import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.db.services.SaleTierService;
import io.iconator.commons.sql.dao.EligibleForRefundRepository;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.commons.sql.dao.PaymentLogRepository;
import io.iconator.monitor.EthereumMonitor;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.TokenConversionService;
import io.iconator.monitor.utils.MockICOnatorMessageService;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import javax.persistence.EntityManagerFactory;

@Configuration
@EnableJpaRepositories("io.iconator.commons.sql.dao")
@EntityScan(basePackages = {"io.iconator.commons.model"})
public class EthereumMonitorTestConfig {

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService("http://127.0.0.1:8545/rpc"));
    }

    @Bean
    public TokenConversionService tokenConversionService() {
        return new TokenConversionService();
    }

    @Bean
    public EthereumMonitor ethereumMonitor(Web3j web3j,
                                           FxService fxService,
                                           TokenConversionService tokenConversionService,
                                           InvestorRepository investorRepository,
                                           PaymentLogRepository paymentLogRepository,
                                           EligibleForRefundRepository eligibleForRefundRepository,
                                           ICOnatorMessageService messageService) {
        return new EthereumMonitor(
                fxService,
                investorRepository,
                paymentLogRepository,
                tokenConversionService,
                eligibleForRefundRepository,
                messageService,
                web3j
        );
    }

    @Bean
    public MockICOnatorMessageService mockICOnatorMessageService() {
        return new MockICOnatorMessageService();
    }

    @Bean
    public ICOnatorMessageService messageService(MockICOnatorMessageService mockICOnatorMessageService) {
        return mockICOnatorMessageService;
    }

    @Bean
    public SaleTierService saleTierService() {
        return new SaleTierService();
    }

    @Bean
    public MonitorAppConfig monitorAppConfig() {
        return new MonitorAppConfig();
    }
}
