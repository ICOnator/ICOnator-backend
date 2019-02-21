package io.iconator.rates;

import io.iconator.commons.baseservice.ConfigNaming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import static org.springframework.boot.SpringApplication.run;

/**
 * Running the rates application starts a scheduled task defined in
 * {@link io.iconator.rates.task.FetchCurrentRatesRunnable} and configured in
 * {@link io.iconator.rates.config.ScheduleConfig#configureTasks(ScheduledTaskRegistrar)}.
 */
@SpringBootApplication(exclude = RepositoryRestMvcAutoConfiguration.class)
@EntityScan({"io.iconator.commons.model.db"})
@EnableJpaRepositories({"io.iconator.commons.sql.dao"})
@EnableAutoConfiguration
@ComponentScan({"io.iconator.commons.sql.dao", "io.iconator.commons.auth",
        "io.iconator.rates", "io.iconator.commons.cryptocompare"})
public class RatesApplication {
    static { ConfigNaming.set("rates.application"); }
    private static final Logger LOG = LoggerFactory.getLogger(RatesApplication.class);

    public static void main(String[] args) {
        try {
            run(RatesApplication.class, args);
        } catch (Throwable t) {
            //ignore silent exception
            if (!t.getClass().toString().endsWith("SilentExitException")) {
                LOG.error("Cannot execute rates.", t);
            }
        }
    }

}
