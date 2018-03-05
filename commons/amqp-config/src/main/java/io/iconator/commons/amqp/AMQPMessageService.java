package io.iconator.commons.amqp;

public interface AMQPMessageService {

    void send(String routingKey, Object message);

}
