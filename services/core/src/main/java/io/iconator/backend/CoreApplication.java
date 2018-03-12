package io.iconator.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableJpaRepositories("io.iconator.commons.sql.dao")
@EntityScan("io.iconator.commons.model.db")
public class CoreApplication {

    private static final Logger LOG = LoggerFactory.getLogger(CoreApplication.class);


    public static void main(String[] args) {
        try {
            run(CoreApplication.class, args);
        } catch (Throwable t) {
            LOG.error("cannot execute core", t);
        }
    }

}
