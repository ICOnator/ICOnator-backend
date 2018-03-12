package io.iconator.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("io.iconator.commons.sql.dao")
@EntityScan("io.iconator.commons.model.db")
public class MonitorApplication {
    private static final Logger LOG = LoggerFactory.getLogger(MonitorApplication.class);

    public static void main(String[] args) throws Exception {
        try {
            new SpringApplicationBuilder(MonitorApplication.class).run(args);
        } catch (Throwable t) {
            LOG.error("cannot execute monitor", t);
        }
    }

}
