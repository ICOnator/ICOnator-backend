package io.iconator.monitor.config;

import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.db.services.EligibleForRefundService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.db.services.SaleTierService;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.monitor.EthereumMonitor;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.MonitorService;
import io.iconator.monitor.utils.MockICOnatorMessageService;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Configuration
@EnableJpaRepositories("io.iconator.commons.sql.dao")
@EntityScan(basePackages = {"io.iconator.commons.model"})
public class EthereumMonitorTestConfig {

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService("http://127.0.0.1:8545/rpc"));
    }

    @Bean
    public MonitorService tokenConversionService() {
        return new MonitorService();
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
    public FxService fxService() {
        return new FxService();
    }

    @Bean
    public EthereumMonitor ethereumMonitor(Web3j web3j,
                                           FxService fxService,
                                           MonitorService monitorService,
                                           InvestorRepository investorRepository,
                                           PaymentLogService paymentLogService,
                                           EligibleForRefundService eligibleForRefundService,
                                           ICOnatorMessageService messageService) {
        return new EthereumMonitor(
                fxService,
                investorRepository,
                paymentLogService,
                monitorService,
                eligibleForRefundService,
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
