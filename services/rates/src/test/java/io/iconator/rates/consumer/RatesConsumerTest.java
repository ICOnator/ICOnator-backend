package io.iconator.rates.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.amqp.model.FetchRatesRequestMessage;
import io.iconator.commons.amqp.model.FetchRatesResponseMessage;
import io.iconator.commons.cryptocompare.CryptoCompareClientService;
import io.iconator.commons.cryptocompare.config.CryptoCompareConfig;
import io.iconator.commons.cryptocompare.config.CryptoCompareConfigHolder;
import io.iconator.commons.model.CurrencyType;
import io.iconator.rates.config.AggregationServiceConfig;
import io.iconator.rates.config.RaterConsumerTestConfig;
import io.iconator.rates.config.RatesAppConfigHolder;
import io.iconator.rates.config.TestConfig;
import io.iconator.rates.service.RatesProviderService;
import io.iconator.rates.service.exceptions.RateNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        RatesConsumer.class,
        RaterConsumerTestConfig.class
})
@TestPropertySource({"classpath:rates.application.properties", "classpath:application-test.properties"})
public class RatesConsumerTest {

    private static final Logger LOG = LoggerFactory.getLogger(RatesConsumerTest.class);

    @Autowired
    private RatesConsumer ratesConsumer;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RatesProviderService ratesProviderService;

    @Test
    public void testReceiveMessage() throws RateNotFoundException, JsonProcessingException {

        Instant now = Instant.now();
        FetchRatesRequestMessage requestMessage = new FetchRatesRequestMessage(
                Date.from(now), CurrencyType.USD, Arrays.asList(CurrencyType.ETH));

        when(ratesProviderService.getRate(eq(CurrencyType.ETH), any())).thenReturn(new BigDecimal("1.1234567"));

        FetchRatesResponseMessage responseMessage = ratesConsumer.receiveMessage(requestMessage);

        assertNotNull(responseMessage);
        assertTrue(responseMessage.getFrom() == CurrencyType.USD);
        assertTrue(responseMessage.getExchangeAggregateRates()
                .stream().allMatch((element) -> element.getCurrencyType().equals(CurrencyType.ETH)));
        assertTrue(responseMessage.getExchangeAggregateRates()
                .stream().allMatch((element) -> element.getAggregateRateDate().equals(Date.from(now))));
        assertTrue(responseMessage.getExchangeAggregateRates()
                .stream().allMatch((element) -> element.getAggregateExchangeRate().equals(new BigDecimal("1.1234567"))));
    }

}
