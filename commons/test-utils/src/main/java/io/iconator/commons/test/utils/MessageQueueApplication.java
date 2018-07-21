package io.iconator.commons.test.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
public class MessageQueueApplication {

    private static final Logger LOG = LoggerFactory.getLogger(MessageQueueApplication.class);

    public static void main(String[] args) {
        try {
            run(MessageQueueApplication.class, args);
        } catch (Throwable t) {
            LOG.error("cannot execute core", t);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() throws Exception {
        BuiltInMessageBroker.start();
    }
}
