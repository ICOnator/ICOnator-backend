package io.iconator.commons.amqp;

import org.slf4j.Logger;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;

final class AsyncAMQPMessageService implements AMQPMessageService {

    private final static Logger LOG = getLogger(AsyncAMQPMessageService.class);
    private static final long DEFAULT_SHUTDOWN_GRACE_TIME = SECONDS.toMillis(10);

    private final ExecutorService executorService;
    private final AMQPMessageService amqpService;
    private final long shutdownGraceTimeInMillis;

    public AsyncAMQPMessageService(AMQPMessageService amqpService) {
        this(getDefaultExecutorService(), amqpService, DEFAULT_SHUTDOWN_GRACE_TIME);
    }

    private static ExecutorService getDefaultExecutorService() {
        return new ThreadPoolExecutor(1, 8, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    AsyncAMQPMessageService(ExecutorService executorService, AMQPMessageService delegate, long shutdownGraceTimeInMillis) {
        this.executorService = executorService;
        this.amqpService = delegate;
        this.shutdownGraceTimeInMillis = shutdownGraceTimeInMillis;
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        this.executorService.shutdown();
        this.executorService.awaitTermination(this.shutdownGraceTimeInMillis, MILLISECONDS);
        this.executorService.shutdownNow();
    }

    @Override
    public void send(final String routingKey, final Object message) {
        executorService.execute(() -> {
            try {
                LOG.debug("Sending msg to {}. Contents: {}", routingKey, message);
                amqpService.send(routingKey, message);
            } catch (Exception e) {
                LOG.error("Not able to send async message to " + routingKey + ". Error message:" + message, e);
            }
        });
    }

}
