package io.iconator.monitor.config;

import io.iconator.commons.amqp.model.ConfirmationEmailMessage;
import io.iconator.commons.amqp.model.FundsReceivedEmailMessage;
import io.iconator.commons.amqp.model.SetWalletAddressMessage;
import io.iconator.commons.amqp.model.SummaryEmailMessage;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.sql.dao.EligibleForRefundRepository;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.commons.sql.dao.PaymentLogRepository;
import io.iconator.commons.sql.dao.SaleTierRepository;
import io.iconator.monitor.BaseMonitor;
import io.iconator.monitor.EthereumMonitor;
import io.iconator.monitor.service.FxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

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
    public EthereumMonitor ethereumMonitor(SaleTierRepository saleTierRepository,
                                       InvestorRepository investorRepository,
                                       PaymentLogRepository paymentLogRepository,
                                       EligibleForRefundRepository eligibleForRefundRepository,
                                       FxService fxService) {
        Web3j web3j = Web3j.build(new HttpService("http://127.0.0.1:8545/rpc"));
        return new EthereumMonitor(fxService, web3j, investorRepository, paymentLogRepository, saleTierRepository,
                eligibleForRefundRepository, new ICOnatorMessageService() {
            @Override
            public void send(ConfirmationEmailMessage confirmationEmailMessage) {
                System.out.println("send1");
            }

            @Override
            public void send(SummaryEmailMessage summaryEmailMessage) {
                System.out.println("send2");
            }

            @Override
            public void send(FundsReceivedEmailMessage fundsReceivedEmailMessage) {
                System.out.println("send3");
            }

            @Override
            public void send(SetWalletAddressMessage newPayInAddressesMessage) {
                System.out.println("send4");
            }
        });
    }

    @Bean
    public MonitorAppConfig monitorAppConfig() {
        return new MonitorAppConfig();
    }
}

