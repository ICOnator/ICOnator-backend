package io.iconator.local;

import io.iconator.core.CoreApplication;
import io.iconator.email.EmailApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.PropertySource;


@SpringBootApplication
@PropertySource({
        "core.application.properties",
        "email.application.properties",
        "monitor.application.properties",
        "rates.application.properties"})
public class ICOnatorApplication {
    private static final Logger LOG = LoggerFactory.getLogger(ICOnatorApplication.class);

    public static void main(String[] args) throws Exception {
        try {
            startMessageQueue();
            startDummySmtp();
            //SpringApplication.run(EmailApplication.class, args);
            new SpringApplicationBuilder().sources(CoreApplication.class, EmailApplication.class).run(args);
            //SpringApplication.run(CoreApplication.class, args);
            //SpringApplication.run(MonitorApplication.class, args);
            //SpringApplication.run(RatesApplication.class, args);
        } catch (Throwable t) {
            //ignore silent exception
            if(!t.getClass().toString().endsWith("SilentExitException")) {
                LOG.error("cannot execute monitor", t);
            }
        }
    }

    /**
     * Starts the message queue when no profile is set, this means we are local
     * and we want to start the message queue on this machine. Also, the dev-tools
     * with Spring Boot, restart the application, thus, we don't want to restart the queue.
     *
     * @throws Exception
     */
    private static void startMessageQueue() throws Exception {
        if(!Thread.currentThread().getName().equals("restartedMain")) {
            io.iconator.commons.test.utils.BuiltInMessageBroker.start();
        }
    }

    /**
     * Starts the dummy email service. The emails will be shown on the console
     *
     * @throws Exception
     */
    private static void startDummySmtp() throws Exception {
        if(!Thread.currentThread().getName().equals("restartedMain")) {
            io.iconator.commons.test.utils.DummySmtp.start();
        }
    }
}
