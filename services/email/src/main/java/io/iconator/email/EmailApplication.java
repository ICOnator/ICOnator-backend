package io.iconator.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
public class EmailApplication {
    private static final Logger LOG = LoggerFactory.getLogger(EmailApplication.class);

    public static void main(String[] args) {
        try {
            run(EmailApplication.class, args);
        } catch (Throwable t) {
            LOG.error("cannot execute core", t);
        }
    }

}
