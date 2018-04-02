package io.iconator.rates.config;

import io.iconator.rates.task.FetchRatesRunnable;
import io.iconator.rates.task.FetchRatesTrigger;
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
@Import({RatesAppConfig.class, FetchRatesRunnable.class, FetchRatesTrigger.class})
@EnableScheduling
public class ScheduleConfig implements SchedulingConfigurer {

    @Autowired
    private FetchRatesRunnable fetchRatesRunnable;

    @Autowired
    private FetchRatesTrigger fetchRatesTrigger;

    @Autowired
    private RatesAppConfig ratesAppConfig;

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService taskExecutor() {
        // TODO: 01.04.18 Guil:
        // Just create the executor if the "periodic fetch rates feature" is enabled.
        return Executors.newScheduledThreadPool(100);
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        if (this.ratesAppConfig.getPeriodicEnabled()) {
            taskRegistrar.setScheduler(taskExecutor());
            taskRegistrar.addTriggerTask(
                    this.fetchRatesRunnable,
                    this.fetchRatesTrigger
            );
        }
    }

}
