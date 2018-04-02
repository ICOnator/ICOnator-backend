package io.iconator.core;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableJpaRepositories("io.iconator.commons.sql.dao")
@EntityScan("io.iconator.commons.model.db")

//required for swagger to find the configuration (baseservice) and the API methods (controller)
@ComponentScan({"io.iconator.commons.baseservice", "io.iconator.core"})
@PropertySource("core.application-${spring.profiles.active:default}.properties")
public class CoreApplication {

    private static final Logger LOG = LoggerFactory.getLogger(CoreApplication.class);

    public static void main(String[] args) {
        try {
            startMessageQueue();
            run(CoreApplication.class, args);
        } catch (Throwable t) {
            //ignore silent exception
            if(!t.getClass().toString().endsWith("SilentExitException")) {
                LOG.error("cannot execute core", t);
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
        String profile = System.getenv("spring.profiles.active");
        if(Strings.isNullOrEmpty(profile)
                && !Thread.currentThread().getName().equals("restartedMain")) {
            io.iconator.commons.test.utils.BuiltInMessageBroker.start();
        }
    }
}
