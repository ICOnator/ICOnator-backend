package io.iconator.kyc;

import io.iconator.commons.baseservice.ConfigNaming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableJpaRepositories({"io.iconator.commons.sql.dao"})
@EntityScan({"io.iconator.commons.model.db"})
@ComponentScan({
        "io.iconator.commons.auth",
        "io.iconator.kyc"
})
public class KycApplication {

    static {
        ConfigNaming.set("kyc.application");
    }

    private static final Logger LOG = LoggerFactory.getLogger(KycApplication.class);

    public static void main(String[] args) {
        try {
            run(KycApplication.class, args);
        } catch (Throwable t) {
            //ignore silent exception
            if (!t.getClass().toString().endsWith("SilentExitException")) {
                LOG.error("cannot execute kyc", t);
            }
        }
    }

}
