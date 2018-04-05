package io.iconator.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableJpaRepositories({"io.iconator.commons.sql.dao"})
@EntityScan({"io.iconator.commons.model.db"})
@ComponentScan({"io.iconator.commons.baseservice", "io.iconator.commons.security", "io.iconator.core"})
@PropertySources({
        @PropertySource(value = "classpath:core.application.properties"),
        @PropertySource(value = "classpath:core.application-${spring.profiles.active}.properties", ignoreResourceNotFound = true)
})
public class CoreApplication {

    private static final Logger LOG = LoggerFactory.getLogger(CoreApplication.class);

    public static void main(String[] args) {
        try {
            run(CoreApplication.class, args);
        } catch (Throwable t) {
            //ignore silent exception
            if(!t.getClass().toString().endsWith("SilentExitException")) {
                LOG.error("cannot execute core", t);
            }
        }
    }
}
