package io.iconator.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.amqp.RabbitMQMessageServiceFactory;
import io.iconator.commons.amqp.config.AMQPConnectionConfig;
import org.springframework.amqp.core.Exchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class MessageBrokerTestConfig {

    @Bean
    public AMQPConnectionConfig hookRabbitMQService(@Value("${io.iconator.commons.amqp.url}") String rabbitMQConnectionString) {
        return new AMQPConnectionConfig(rabbitMQConnectionString);
    }

    @Bean
    public Exchange declareHookExchange(AMQPConnectionConfig amqpConnectionConfig) {
        final RabbitMQMessageServiceFactory rabbitMQNotificationServiceFactory = notificationServiceFactory();
        rabbitMQNotificationServiceFactory.createAsyncService(amqpConnectionConfig);
        Optional<Exchange> oExchangeObject = rabbitMQNotificationServiceFactory.getExchangeObject();
        return oExchangeObject.get();
    }

    private RabbitMQMessageServiceFactory notificationServiceFactory() {
        return new RabbitMQMessageServiceFactory(defaultObjectMapper());
    }

    private ObjectMapper defaultObjectMapper() {
        return new ObjectMapper();
    }

}
