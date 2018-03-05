package io.iconator.commons.amqp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.amqp.config.AMQPConfig;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public final class RabbitMQMessageServiceFactory {

    private final ObjectMapper objectMapper;
    private URI amqpConnectionURI;

    public RabbitMQMessageServiceFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AMQPMessageService createAsyncService(AMQPConfig mqUserProvidedService) {
        AMQPMessageService syncService = createService(mqUserProvidedService);
        return new AsyncAMQPMessageService(syncService);
    }

    public AMQPMessageService createService(AMQPConfig amqpConfig) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate();
        this.amqpConnectionURI = amqpConfig.getURI();
        configureConnectionFactory(rabbitTemplate, this.amqpConnectionURI);
        configureExchangeName(rabbitTemplate);
        configureMessageConverter(rabbitTemplate);
        return new SpringAMQPMessageService(rabbitTemplate);
    }

    private void configureConnectionFactory(RabbitTemplate rabbitTemplate, URI rabbitMQConnectionURI) {
        rabbitTemplate.setConnectionFactory(new CachingConnectionFactory(rabbitMQConnectionURI));
    }

    private void configureMessageConverter(RabbitTemplate rabbitTemplate) {
        final MessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        rabbitTemplate.setMessageConverter(converter);
    }

    private void configureExchangeName(RabbitTemplate rabbitTemplate) {
        ofNullable(getExchangeName()).ifPresent((exchange) -> {
            rabbitTemplate.setExchange(exchange);
        });
    }

    private String getExchangeName() {
        return getQueryParams(amqpConnectionURI).getFirst("exchangeName");
    }

    private String getExchangeType() {
        return getQueryParams(amqpConnectionURI).getFirst("exchangeType");
    }

    private String getDurable() {
        return getQueryParams(amqpConnectionURI).getFirst("durable");
    }

    private String getAutoDelete() {
        return getQueryParams(amqpConnectionURI).getFirst("autoDelete");
    }

    private MultiValueMap<String, String> getQueryParams(URI rabbitMQConnectionURI) {
        UriComponents uriComponents = UriComponentsBuilder.fromUri(rabbitMQConnectionURI).build();
        MultiValueMap<String, String> queryParams = uriComponents.getQueryParams();
        return queryParams;
    }

    public Optional<Exchange> getExchangeObject() {
        boolean durableBool = getDurable() != null ? Boolean.parseBoolean(getDurable()) : false;
        boolean autoDeleteBool = getAutoDelete() != null ? Boolean.parseBoolean(getAutoDelete()) : false;
        String exchangeType = getExchangeType() != null ? getExchangeType() : ExchangeTypes.TOPIC;
        String exchangeName = getExchangeName();
        if (ofNullable(exchangeName).isPresent()) {
            switch (exchangeType.toLowerCase()) {
                case ExchangeTypes.TOPIC:
                    return Optional.of(new TopicExchange(exchangeName, durableBool, autoDeleteBool));
                case ExchangeTypes.FANOUT:
                    return Optional.of(new FanoutExchange(exchangeName, durableBool, autoDeleteBool));
                case ExchangeTypes.DIRECT:
                    return Optional.of(new DirectExchange(exchangeName, durableBool, autoDeleteBool));
                default:
                    return Optional.of(new TopicExchange(exchangeName, durableBool, autoDeleteBool));
            }
        }
        return Optional.empty();
    }


}
