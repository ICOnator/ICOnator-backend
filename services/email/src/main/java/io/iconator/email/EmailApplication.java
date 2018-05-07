package io.iconator.email;

import io.iconator.commons.baseservice.ConfigNaming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@ComponentScan({"io.iconator.commons.auth", "io.iconator.email"})
public class EmailApplication {
    static { ConfigNaming.set("email.application"); }
    private static final Logger LOG = LoggerFactory.getLogger(EmailApplication.class);

    public static void main(String[] args) {
        try {
            run(EmailApplication.class, args);
        } catch (Throwable t) {
            //ignore silent exception
            if (!t.getClass().toString().endsWith("SilentExitException")) {
                LOG.error("cannot execute core", t);
            }
        }
    }

}
