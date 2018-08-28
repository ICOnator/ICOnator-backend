package io.iconator.rates.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.amqp.model.FetchRatesRequestMessage;
import io.iconator.commons.amqp.model.FetchRatesResponseMessage;
import io.iconator.commons.amqp.model.dto.ExchangeAggregateCurrencyRateDTO;
import io.iconator.commons.model.CurrencyType;
import io.iconator.rates.service.RatesProviderService;
import io.iconator.rates.service.exceptions.RateNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.iconator.commons.amqp.model.constants.ExchangeConstants.ICONATOR_ENTRY_EXCHANGE;
import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.RATES_EXCHANGE_REQUEST_ROUTING_KEY;
import static java.util.Optional.ofNullable;

@Service
public class RatesConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(RatesConsumer.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RatesProviderService ratesProviderService;

    @RabbitListener(
            bindings = @QueueBinding(value = @Queue,
                    exchange = @Exchange(
                            value = ICONATOR_ENTRY_EXCHANGE,
                            type = ExchangeTypes.TOPIC,
                            ignoreDeclarationExceptions = "true",
                            durable = "true"
                    ),
                    key = RATES_EXCHANGE_REQUEST_ROUTING_KEY)
    )
    public FetchRatesResponseMessage receiveMessage(FetchRatesRequestMessage messageObject) {
        CurrencyType from = ofNullable(messageObject)
                .map((m) -> m.getFrom())
                .orElseThrow(() -> new AmqpRejectAndDontRequeueException("'from' attribute on FetchRatesRequestMessage message is null."));

        List<CurrencyType> to = ofNullable(messageObject).map((m) -> m.getTo()).orElse(Arrays.asList());

        Instant timestamp = ofNullable(messageObject)
                .map((m) -> m.getDesiredRateTimestamp())
                .map((date) -> date.toInstant())
                .orElseThrow(() -> new AmqpRejectAndDontRequeueException("'desiredRateTimestamp' attribute on " +
                        "FetchRatesRequestMessage message is null or has a wrong format."));

        List<ExchangeAggregateCurrencyRateDTO> listDTOs = to.stream().map((currency) -> {
            try {
                BigDecimal rate = ratesProviderService.getRate(currency, timestamp);
                return new ExchangeAggregateCurrencyRateDTO(Date.from(timestamp), currency, rate);
            } catch (RateNotFoundException e) {
                LOG.error("Rate could not be fetched for {}. Exception: {}", currency, e);
            }
            return null;
        }).filter((element) -> element != null).collect(Collectors.toList());

        return new FetchRatesResponseMessage(from, listDTOs);
    }

}
