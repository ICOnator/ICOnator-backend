package io.iconator.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableJpaRepositories({"io.iconator.commons.sql.dao"})
@EntityScan({"io.iconator.commons.model.db"})
@PropertySources({
        @PropertySource(value = "classpath:monitor.application.properties"),
        @PropertySource(value = "classpath:monitor.application-${spring.profiles.active}.properties", ignoreResourceNotFound = true)
})
public class MonitorApplication {

    private static final Logger LOG = LoggerFactory.getLogger(MonitorApplication.class);

    public static void main(String[] args) throws Exception {
        try {
            run(MonitorApplication.class, args);
        } catch (Throwable t) {
            //ignore silent exception
            if (!t.getClass().toString().endsWith("SilentExitException")) {
                LOG.error("cannot execute monitor", t);
            }
        }
    }

}
