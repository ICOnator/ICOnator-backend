package io.iconator.commons.amqp.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.amqp.AMQPMessageService;
import io.iconator.commons.amqp.RabbitMQMessageServiceFactory;
import io.iconator.commons.amqp.config.AMQPConnectionConfig;
import io.iconator.commons.amqp.service.ConcreteICOnatorMessageService;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class ICOnatorMessageServiceConfig {

    @Bean
    public AMQPConnectionConfig amqpConnectionConfig(@Value("${io.iconator.commons.amqp.url}") String amqpConnectionUri) {
        return new AMQPConnectionConfig(amqpConnectionUri);
    }

    @Bean
    public ICOnatorMessageService iconatorMessageService(AMQPConnectionConfig amqpConnectionConfig) {
        AMQPMessageService amqpMessageService = getMessageService(amqpConnectionConfig);
        return new ConcreteICOnatorMessageService(amqpMessageService);
    }

    @Bean
    public Exchange declareICOnatorExchange(AMQPConnectionConfig amqpConnectionConfig) {
        return declareExchange(amqpConnectionConfig);
    }

    private Exchange declareExchange(AMQPConnectionConfig amqpConnectionConfig) {
        final RabbitMQMessageServiceFactory rabbitMQNotificationServiceFactory = messageServiceFactory();
        rabbitMQNotificationServiceFactory.createAsyncService(amqpConnectionConfig);
        Optional<Exchange> oExchangeObject = rabbitMQNotificationServiceFactory.getExchangeObject();
        return oExchangeObject.map((exchange) -> {
            final RabbitAdmin rabbitAdmin = new RabbitAdmin(new CachingConnectionFactory(amqpConnectionConfig.getURI()));
            rabbitAdmin.declareExchange(exchange);
            return exchange;
        }).orElse(null);
    }

    private AMQPMessageService getMessageService(AMQPConnectionConfig amqpConnectionConfig) {
        final RabbitMQMessageServiceFactory rabbitMQNotificationServiceFactory = messageServiceFactory();
        return rabbitMQNotificationServiceFactory.createAsyncService(amqpConnectionConfig);
    }

    private RabbitMQMessageServiceFactory messageServiceFactory() {
        return new RabbitMQMessageServiceFactory(defaultObjectMapper());
    }

    private ObjectMapper defaultObjectMapper() {
        return new ObjectMapper();
    }

}
