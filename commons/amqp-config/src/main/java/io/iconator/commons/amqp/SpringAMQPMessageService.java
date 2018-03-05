package io.iconator.commons.amqp;

import org.springframework.amqp.core.AmqpTemplate;

final class SpringAMQPMessageService implements AMQPMessageService {

    private AmqpTemplate amqpTemplate;

    SpringAMQPMessageService(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void send(String route, Object message) {
        amqpTemplate.convertAndSend(route, message);
    }
}
