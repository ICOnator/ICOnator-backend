package io.iconator.local;

import io.iconator.commons.baseservice.ConfigNaming;
import io.iconator.core.CoreApplication;
import io.iconator.email.EmailApplication;
import io.iconator.monitor.MonitorApplication;
import io.iconator.rates.RatesApplication;
import io.iconator.testrpcj.TestBlockchain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ICOnatorLocalDevApplication {

    static {
        ConfigNaming.set(
                "core.application",
                "email.application",
                "monitor.application",
                "rates.application",
                "application");
    }

    private static final Logger LOG = LoggerFactory.getLogger(ICOnatorLocalDevApplication.class);

    public static void main(String[] args) throws Exception {
        try {
            startMessageQueue();
            startDummySmtp();
            startEthereumTest();
            new SpringApplicationBuilder().sources(
                    CoreApplication.class,
                    EmailApplication.class,
                    MonitorApplication.class,
                    RatesApplication.class,
                    io.iconator.testrpcj.TestBlockchain.class).run(args);
        } catch (Throwable t) {
            //ignore silent exception
            if (!t.getClass().toString().endsWith("SilentExitException")) {
                LOG.error("cannot execute monitor", t);
            }
        }
    }

    /**
     * Starts the message queue broker when no profile is set, this means we are local
     * and we want to start the broker on this machine. Also, the dev-tools
     * with Spring Boot, restart the application, thus, we don't want to restart the queue.
     *
     * @throws Exception
     */
    private static void startMessageQueue() throws Exception {
        if (!Thread.currentThread().getName().equals("restartedMain")) {
            io.iconator.commons.test.utils.BuiltInMessageBroker.start();
        }
    }

    /**
     * Starts the dummy email service. The emails will be shown on the console
     *
     * @throws Exception
     */
    private static void startDummySmtp() throws Exception {
        if (!Thread.currentThread().getName().equals("restartedMain")) {
            io.iconator.commons.test.utils.DummySmtp.start();
        }
    }

    private static void startEthereumTest() throws Exception {
        if (!Thread.currentThread().getName().equals("restartedMain")) {
            TestBlockchain.run();
        }
    }

}
