package io.iconator.monitor.config;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.db.services.EligibleForRefundService;
import io.iconator.commons.db.services.InvestorService;
import io.iconator.commons.db.services.PaymentLogService;
import io.iconator.commons.db.services.SaleTierService;
import io.iconator.commons.model.db.PaymentLog;
import io.iconator.commons.sql.dao.InvestorRepository;
import io.iconator.monitor.EthereumMonitor;
import io.iconator.monitor.service.FxService;
import io.iconator.monitor.service.MonitorService;
import io.iconator.monitor.utils.MockICOnatorMessageService;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import javax.persistence.OptimisticLockException;
import java.util.concurrent.TimeUnit;

import static com.github.rholder.retry.WaitStrategies.randomWait;

@Configuration
@EnableJpaRepositories("io.iconator.commons.sql.dao")
@EntityScan(basePackages = {"io.iconator.commons.model"})
public class MonitorTestConfig {

    @Bean
    public FxService fxService() {
        return new FxService();
    }

    @Bean
    public MonitorService monitorService() {
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
    public MockICOnatorMessageService mockICOnatorMessageService() {
        return new MockICOnatorMessageService();
    }

    @Bean
    public MonitorAppConfigHolder monitorAppConfigHolder() {
        return new MonitorAppConfigHolder();
    }

    @Bean
    public SaleTierService saleTierService() {
        return new SaleTierService();
    }

    @Bean
    public InvestorService investorService(InvestorRepository investorRepository) {
        return new InvestorService(investorRepository);
    }

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService("http://127.0.0.1:8545/rpc"));
    }

    @Bean
    public ICOnatorMessageService messageService(MockICOnatorMessageService mockICOnatorMessageService) {
        return mockICOnatorMessageService;
    }

    @Bean
    public Retryer<PaymentLog> retryer() {
        return RetryerBuilder.<PaymentLog>newBuilder()
                .retryIfExceptionOfType(OptimisticLockingFailureException.class)
                .retryIfExceptionOfType(OptimisticLockException.class)
                .withWaitStrategy(randomWait(1000L, TimeUnit.MILLISECONDS))
                .withStopStrategy(StopStrategies.neverStop())
                .build();
    }

    @Bean
    public EthereumMonitor ethereumMonitor(Web3j web3j,
                                           FxService fxService,
                                           MonitorService monitorService,
                                           PaymentLogService paymentLogService,
                                           ICOnatorMessageService messageService,
                                           InvestorService investorService,
                                           MonitorAppConfigHolder configHolder,
                                           Retryer retryer) {
        return new EthereumMonitor(
                fxService,
                paymentLogService,
                monitorService,
                messageService,
                investorService,
                web3j,
                configHolder,
                retryer
        );
    }

}

