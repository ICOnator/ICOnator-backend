package io.iconator.email.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.amqp.config.AMQPConnectionConfig;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class EmailAppConfig {

    @Bean
    public ConnectionFactory connectionFactory(AMQPConnectionConfig amqpConnectionConfig) {
        return new CachingConnectionFactory(amqpConnectionConfig.getURI());
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        // TODO: 05.03.18 Guil:
        // Set these static values on the applications.properties
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setReplyTimeout(10000);

        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(500);
        backOffPolicy.setMultiplier(10.0);
        backOffPolicy.setMaxInterval(10000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        rabbitTemplate.setRetryTemplate(retryTemplate);

        final MessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        rabbitTemplate.setMessageConverter(converter);

        return rabbitTemplate;
    }

}
