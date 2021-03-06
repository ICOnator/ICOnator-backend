package io.iconator.monitor;

import io.iconator.commons.baseservice.ConfigNaming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication(exclude = RepositoryRestMvcAutoConfiguration.class)
@EnableJpaRepositories({"io.iconator.commons.sql.dao"})
@EntityScan({"io.iconator.commons.model.db"})
@ComponentScan({"io.iconator.commons.auth",
        "io.iconator.monitor",
        "io.iconator.commons.db.services"})
@EnableScheduling
public class MonitorApplication {
    static { ConfigNaming.set("monitor.application"); }
    private static final Logger LOG = LoggerFactory.getLogger(MonitorApplication.class);

    public static void main(String[] args) throws Exception {
        try {
            run(MonitorApplication.class, args);
        } catch (Throwable t) {
            //ignore silent exception
            if (!t.getClass().toString().endsWith("SilentExitException")) {
                LOG.error("Cannot execute monitor.", t);
            }
        }
    }

}
