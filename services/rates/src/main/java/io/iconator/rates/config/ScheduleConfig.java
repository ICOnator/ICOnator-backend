package io.iconator.rates.config;

import io.iconator.rates.task.FetchCurrentRatesRunnable;
import io.iconator.rates.task.FetchCurrentRatesTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@Import({RatesAppConfigHolder.class, FetchCurrentRatesRunnable.class, FetchCurrentRatesTrigger.class})
@EnableScheduling
public class ScheduleConfig implements SchedulingConfigurer {

    @Autowired
    private FetchCurrentRatesRunnable fetchRatesRunnable;

    @Autowired
    private FetchCurrentRatesTrigger fetchRatesTrigger;

    @Autowired
    private RatesAppConfigHolder ratesAppConfigHolder;

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService taskExecutor() {
        return Executors.newScheduledThreadPool(100);
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        if (this.ratesAppConfigHolder.getCurrentPeriodicEnabled()) {
            taskRegistrar.setScheduler(taskExecutor());
            taskRegistrar.addTriggerTask(
                    this.fetchRatesRunnable,
                    this.fetchRatesTrigger
            );
        }
    }

}
