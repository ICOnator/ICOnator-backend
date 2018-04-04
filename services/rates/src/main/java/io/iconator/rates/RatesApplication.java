package io.iconator.rates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EntityScan({"io.iconator.commons.model.db"})
@EnableJpaRepositories({"io.iconator.commons.sql.dao"})
@EnableAutoConfiguration
@ComponentScan({"io.iconator.commons.sql.dao", "io.iconator.rates"})
@PropertySources({
        @PropertySource(value = "classpath:rates.application.properties"),
        @PropertySource(value = "classpath:rates.application-${spring.profiles.active}.properties", ignoreResourceNotFound = true)
})
public class RatesApplication {

    private static final Logger LOG = LoggerFactory.getLogger(RatesApplication.class);

    public static void main(String[] args) {
        try {
            run(RatesApplication.class, args);
        } catch (Throwable t) {
            //ignore silent exception
            if (!t.getClass().toString().endsWith("SilentExitException")) {
                LOG.error("cannot execute rates", t);
            }
        }
    }

}
