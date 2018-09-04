package io.iconator.core;

import io.iconator.commons.baseservice.ConfigNaming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication(exclude = RepositoryRestMvcAutoConfiguration.class)
@EnableJpaRepositories({"io.iconator.commons.sql.dao"})
@EntityScan({"io.iconator.commons.model.db"})
@ComponentScan({
        "io.iconator.commons.baseservice",
        "io.iconator.commons.auth",
        "io.iconator.commons.db.services",
        "io.iconator.commons.recaptcha",
        "io.iconator.commons.countryfilter",
        "io.iconator.core"
})
public class CoreApplication {

    static {
        ConfigNaming.set("core.application");
    }

    private static final Logger LOG = LoggerFactory.getLogger(CoreApplication.class);

    public static void main(String[] args) {
        try {
            run(CoreApplication.class, args);
        } catch (Throwable t) {
            //ignore silent exception
            if (!t.getClass().toString().endsWith("SilentExitException")) {
                LOG.error("Cannot execute core.", t);
            }
        }
    }
}
