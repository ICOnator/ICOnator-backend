package io.iconator.commons.amqp.config;

import java.net.URI;

import static java.util.Objects.requireNonNull;

public class AMQPConnectionConfig implements AMQPConfig {

    private final URI amqpConnectionString;

    public AMQPConnectionConfig(String amqpConnectionString) {
        String uriStr = requireNonNull(amqpConnectionString);
        this.amqpConnectionString = URI.create(uriStr);
    }

    public final URI getURI() {
        return amqpConnectionString;
    }

    @Override
    public String toString() {
        return getClass() + "{"
                + "amqpConnectionString=" + amqpConnectionString
                + '}';
    }
}
