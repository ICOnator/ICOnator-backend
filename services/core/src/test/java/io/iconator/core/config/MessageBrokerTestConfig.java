package io.iconator.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.amqp.RabbitMQMessageServiceFactory;
import io.iconator.commons.amqp.config.AMQPConnectionConfig;
import io.iconator.commons.amqp.service.config.ICOnatorMessageServiceConfig;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Optional;

@Configuration
@Import(ICOnatorMessageServiceConfig.class)
public class MessageBrokerTestConfig {

    @Bean
    public AMQPConnectionConfig hookRabbitMQService(@Value("${io.iconator.commons.amqp.url}") String rabbitMQConnectionString) {
        return new AMQPConnectionConfig(rabbitMQConnectionString);
    }

    @Bean
    public Exchange declareHookExchange(AMQPConnectionConfig amqpConnectionConfig, RabbitTemplate rabbitTemplate) {
        final RabbitMQMessageServiceFactory rabbitMQNotificationServiceFactory = messageServiceFactory(rabbitTemplate);
        rabbitMQNotificationServiceFactory.createAsyncService(amqpConnectionConfig);
        Optional<Exchange> oExchangeObject = rabbitMQNotificationServiceFactory.getExchangeObject();
        return oExchangeObject.get();
    }

    private RabbitMQMessageServiceFactory messageServiceFactory(RabbitTemplate rabbitTemplate) {
        return new RabbitMQMessageServiceFactory(rabbitTemplate, defaultObjectMapper());
    }

    private ObjectMapper defaultObjectMapper() {
        return new ObjectMapper();
    }

}
