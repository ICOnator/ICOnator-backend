package io.iconator.backend.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import io.iconator.commons.amqp.model.ConfirmationEmailMessage;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class ConfirmationEmailMessageConsumer implements Consumer {

    private static final Logger LOG = getLogger(ConfirmationEmailMessageConsumer.class);

    private List<ConfirmationEmailMessage> consumedMessages = new ArrayList<>();

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        LOG.info("handleDelivery");
        consumedMessages.add(getObjectMapper().readerFor(ConfirmationEmailMessage.class).readValue(body));
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        LOG.info("handleConsumeOk");
    }

    @Override
    public void handleCancelOk(String consumerTag) {
        LOG.info("handleCancelOk");
    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
        LOG.info("handleCancel");
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        LOG.info("handleShutdownSignal");
    }

    @Override
    public void handleRecoverOk(String consumerTag) {
        LOG.info("handleRecoverOk");
    }

    protected ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    public List<ConfirmationEmailMessage> getConsumedMessages() {
        return consumedMessages;
    }

}
