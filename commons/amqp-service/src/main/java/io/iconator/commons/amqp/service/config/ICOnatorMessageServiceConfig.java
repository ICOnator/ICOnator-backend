package io.iconator.commons.amqp.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.amqp.AMQPMessageService;
import io.iconator.commons.amqp.RabbitMQMessageServiceFactory;
import io.iconator.commons.amqp.config.AMQPConnectionConfig;
import io.iconator.commons.amqp.service.ConcreteICOnatorMessageService;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Optional;

@Configuration
public class ICOnatorMessageServiceConfig {

    @Bean
    public Jackson2JsonMessageConverter converter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public ConnectionFactory connectionFactory(AMQPConnectionConfig amqpConnectionConfig) {
        return new CachingConnectionFactory(amqpConnectionConfig.getURI());
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {

        // TODO: 05.03.18 Guil:
        // Set these static values on the applications.properties

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setReplyTimeout(10000);
        rabbitTemplate.setReceiveTimeout(10000);

        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(500);
        backOffPolicy.setMultiplier(10.0);
        backOffPolicy.setMaxInterval(10000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        rabbitTemplate.setRetryTemplate(retryTemplate);

        return rabbitTemplate;
    }

    @Bean
    public AMQPConnectionConfig amqpConnectionConfig(@Value("${io.iconator.commons.amqp.url}") String amqpConnectionUri) {
        return new AMQPConnectionConfig(amqpConnectionUri);
    }

    @Bean
    public ICOnatorMessageService iconatorMessageService(AMQPConnectionConfig amqpConnectionConfig, RabbitTemplate rabbitTemplate) {
        AMQPMessageService amqpMessageService = getMessageService(amqpConnectionConfig, rabbitTemplate);
        return new ConcreteICOnatorMessageService(amqpMessageService);
    }

    @Bean
    public Exchange declareICOnatorExchange(AMQPConnectionConfig amqpConnectionConfig, RabbitTemplate rabbitTemplate) {
        return declareExchange(amqpConnectionConfig, rabbitTemplate);
    }

    private Exchange declareExchange(AMQPConnectionConfig amqpConnectionConfig, RabbitTemplate rabbitTemplate) {
        final RabbitMQMessageServiceFactory rabbitMQNotificationServiceFactory = messageServiceFactory(rabbitTemplate);
        rabbitMQNotificationServiceFactory.createAsyncService(amqpConnectionConfig);
        Optional<Exchange> oExchangeObject = rabbitMQNotificationServiceFactory.getExchangeObject();
        return oExchangeObject.map((exchange) -> {
            final RabbitAdmin rabbitAdmin = new RabbitAdmin(new CachingConnectionFactory(amqpConnectionConfig.getURI()));
            rabbitAdmin.declareExchange(exchange);
            return exchange;
        }).orElse(null);
    }

    private AMQPMessageService getMessageService(AMQPConnectionConfig amqpConnectionConfig, RabbitTemplate rabbitTemplate) {
        final RabbitMQMessageServiceFactory rabbitMQNotificationServiceFactory = messageServiceFactory(rabbitTemplate);
        return rabbitMQNotificationServiceFactory.createAsyncService(amqpConnectionConfig);
    }

    private RabbitMQMessageServiceFactory messageServiceFactory(RabbitTemplate rabbitTemplate) {
        return new RabbitMQMessageServiceFactory(rabbitTemplate, defaultObjectMapper());
    }

    private ObjectMapper defaultObjectMapper() {
        return new ObjectMapper();
    }

}
