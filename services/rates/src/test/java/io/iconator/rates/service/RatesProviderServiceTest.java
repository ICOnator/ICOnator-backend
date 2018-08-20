package io.iconator.rates.service;

import io.iconator.commons.cryptocompare.CryptoCompareClientService;
import io.iconator.commons.cryptocompare.config.CryptoCompareConfig;
import io.iconator.commons.cryptocompare.config.CryptoCompareConfigHolder;
import io.iconator.commons.cryptocompare.model.CryptoCompareConversionRateResponse;
import io.iconator.commons.cryptocompare.model.CryptoCompareConversionRates;
import io.iconator.commons.cryptocompare.model.CryptoCompareCurrency;
import io.iconator.commons.cryptocompare.model.CryptoCompareResponse;
import io.iconator.commons.cryptocompare.model.CryptoCompareResponseDTO;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.ExchangeAggregateCurrencyRate;
import io.iconator.commons.model.db.ExchangeAggregateRate;
import io.iconator.commons.sql.dao.ExchangeAggregateRateRepository;
import io.iconator.rates.config.AggregationServiceConfig;
import io.iconator.rates.config.RatesAppConfig;
import io.iconator.rates.config.RatesAppConfigHolder;
import io.iconator.rates.config.TestConfig;
import io.iconator.rates.service.exceptions.RateNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        TestConfig.class,
        AggregationServiceConfig.class,
        CryptoCompareConfig.class,
        CryptoCompareConfigHolder.class,
        CryptoCompareClientService.class,
        RatesAppConfigHolder.class,
        RatesProviderService.class
})
@DataJpaTest
@TestPropertySource({"classpath:rates.application.properties", "classpath:application-test.properties"})
public class RatesProviderServiceTest {

    @MockBean
    private ExchangeAggregateRateRepository exchangeAggregateRateRepository;

    @MockBean
    private CryptoCompareClientService cryptoCompareClientService;

    @Autowired
    private RatesProviderService ratesProviderService;

    @Test
    public void testGetClosestRateToTimestamp_TwoEntriesWithSameDifferenceShouldGetTheHigherOne() {

        Instant now = Instant.now();
        List<ExchangeAggregateRate> rates = Arrays.asList(
                createExchangeAggregateRate(Date.from(now.plusSeconds(1))),
                createExchangeAggregateRate(Date.from(now.minusSeconds(1)))
        );

        Optional<ExchangeAggregateRate> closestRate = ratesProviderService.getClosestRateToTimestamp(now, rates);
        assertTrue(closestRate.isPresent());
        assertTrue(now.isBefore(closestRate.get().getCreationDate().toInstant()));
    }

    @Test
    public void testGetClosestRateToTimestamp_OneEntryOnly() {

        Instant now = Instant.now();
        List<ExchangeAggregateRate> rates = Arrays.asList(
                createExchangeAggregateRate(Date.from(now.plusSeconds(1)))
        );

        Optional<ExchangeAggregateRate> closestRate = ratesProviderService.getClosestRateToTimestamp(now, rates);
        assertTrue(closestRate.isPresent());
        assertTrue(now.isBefore(closestRate.get().getCreationDate().toInstant()));
    }

    @Test
    public void testGetClosestRateToTimestamp_NoEntries() {

        Instant now = Instant.now();
        List<ExchangeAggregateRate> rates = Arrays.asList();

        Optional<ExchangeAggregateRate> closestRate = ratesProviderService.getClosestRateToTimestamp(now, rates);
        assertTrue(!closestRate.isPresent());
    }

    @Test
    public void testGetClosestRateToTimestamp_EntriesWithNullCreationDate() {

        Instant now = Instant.now();
        List<ExchangeAggregateRate> rates = Arrays.asList(
                createExchangeAggregateRate(Date.from(now.plusSeconds(5))),
                createExchangeAggregateRate(Date.from(now.plusSeconds(1))),
                createExchangeAggregateRate(null),
                createExchangeAggregateRate(null),
                createExchangeAggregateRate(Date.from(now.minusSeconds(1)))
        );

        Optional<ExchangeAggregateRate> closestRate = ratesProviderService.getClosestRateToTimestamp(now, rates);
        assertTrue(closestRate.isPresent());
        assertNotNull(closestRate.get().getCreationDate());
        assertTrue(now.isBefore(closestRate.get().getCreationDate().toInstant()));
    }

    @Test
    public void testGetRate_ClosetRate_Available() throws RateNotFoundException {
        Instant now = Instant.now();

        ExchangeAggregateRate r1 = new ExchangeAggregateRate(Date.from(now.minus(30, ChronoUnit.MINUTES)), null, null);
        ExchangeAggregateRate r2 = new ExchangeAggregateRate(Date.from(now.minus(5, ChronoUnit.MINUTES)), null, null);
        ExchangeAggregateRate r3 = new ExchangeAggregateRate(Date.from(now.plus(10, ChronoUnit.MINUTES)), null, null);

        r1.addExchangeAggregateCurrencyRate(new ExchangeAggregateCurrencyRate(CurrencyType.ETH, new BigDecimal("1.23456789")));
        r1.addExchangeAggregateCurrencyRate(new ExchangeAggregateCurrencyRate(CurrencyType.BTC, new BigDecimal("0.123456789")));

        r2.addExchangeAggregateCurrencyRate(new ExchangeAggregateCurrencyRate(CurrencyType.ETH, new BigDecimal("2.23456789")));
        r2.addExchangeAggregateCurrencyRate(new ExchangeAggregateCurrencyRate(CurrencyType.BTC, new BigDecimal("1.123456789")));

        r3.addExchangeAggregateCurrencyRate(new ExchangeAggregateCurrencyRate(CurrencyType.ETH, new BigDecimal("3.23456789")));
        r3.addExchangeAggregateCurrencyRate(new ExchangeAggregateCurrencyRate(CurrencyType.BTC, new BigDecimal("2.123456789")));

        when(exchangeAggregateRateRepository.findAllByCreationDateBetweenOrderByCreationDateDesc(any(), any()))
                .thenReturn(Arrays.asList(r1, r2, r3));

        BigDecimal rate = ratesProviderService.getRate(CurrencyType.ETH, now);

        assertNotNull(rate);
        assertEquals(new BigDecimal("2.23456789"), rate);
    }

    @Test
    public void testGetRate_ClosetRate_Not_Available() throws Exception {
        Instant now = Instant.now();

        ExchangeAggregateRate r1 = new ExchangeAggregateRate(Date.from(now.minus(30, ChronoUnit.MINUTES)), null, null);
        ExchangeAggregateRate r2 = new ExchangeAggregateRate(Date.from(now.minus(5, ChronoUnit.MINUTES)), null, null);
        ExchangeAggregateRate r3 = new ExchangeAggregateRate(Date.from(now.plus(10, ChronoUnit.MINUTES)), null, null);

        when(exchangeAggregateRateRepository.findAllByCreationDateBetweenOrderByCreationDateDesc(any(), any()))
                .thenReturn(Arrays.asList(r1, r2, r3));

        List<CryptoCompareConversionRateResponse> rateResponses = Arrays.asList(
                new CryptoCompareConversionRateResponse(CryptoCompareCurrency.USD, new BigDecimal("9.2345"))
        );
        CryptoCompareConversionRates conversionRates = new CryptoCompareConversionRates(rateResponses);
        List<CryptoCompareResponse> responses = Arrays.asList(new CryptoCompareResponse(CryptoCompareCurrency.ETH, conversionRates));
        CryptoCompareResponseDTO dto = new CryptoCompareResponseDTO(responses);

        when(cryptoCompareClientService.getHistorical(CryptoCompareCurrency.ETH, Arrays.asList(CryptoCompareCurrency.USD), now.getEpochSecond()))
                .thenReturn(dto);

        BigDecimal rate = ratesProviderService.getRate(CurrencyType.ETH, now);

        assertNotNull(rate);
        assertEquals(new BigDecimal("9.2345"), rate);
    }

    @Test
    public void testGetRate_ClosetRate_Not_Available_Empty_ExchangeAggregateRate() throws Exception {
        Instant now = Instant.now();

        when(exchangeAggregateRateRepository.findAllByCreationDateBetweenOrderByCreationDateDesc(any(), any()))
                .thenReturn(Arrays.asList());

        List<CryptoCompareConversionRateResponse> rateResponses = Arrays.asList(
                new CryptoCompareConversionRateResponse(CryptoCompareCurrency.USD, new BigDecimal("9.2345"))
        );
        CryptoCompareConversionRates conversionRates = new CryptoCompareConversionRates(rateResponses);
        List<CryptoCompareResponse> responses = Arrays.asList(new CryptoCompareResponse(CryptoCompareCurrency.ETH, conversionRates));
        CryptoCompareResponseDTO dto = new CryptoCompareResponseDTO(responses);

        when(cryptoCompareClientService.getHistorical(CryptoCompareCurrency.ETH, Arrays.asList(CryptoCompareCurrency.USD), now.getEpochSecond()))
                .thenReturn(dto);

        BigDecimal rate = ratesProviderService.getRate(CurrencyType.ETH, now);

        assertNotNull(rate);
        assertEquals(new BigDecimal("9.2345"), rate);
    }

    @Test(expected = RateNotFoundException.class)
    public void testGetRate_ClosetRate_Nor_HistoricalDate_Are_Available() throws Exception {
        Instant now = Instant.now();

        ExchangeAggregateRate r1 = new ExchangeAggregateRate(Date.from(now.minus(30, ChronoUnit.MINUTES)), null, null);
        ExchangeAggregateRate r2 = new ExchangeAggregateRate(Date.from(now.minus(5, ChronoUnit.MINUTES)), null, null);
        ExchangeAggregateRate r3 = new ExchangeAggregateRate(Date.from(now.plus(10, ChronoUnit.MINUTES)), null, null);

        when(exchangeAggregateRateRepository.findAllByCreationDateBetweenOrderByCreationDateDesc(any(), any()))
                .thenReturn(Arrays.asList(r1, r2, r3));

        when(cryptoCompareClientService.getHistorical(CryptoCompareCurrency.ETH, Arrays.asList(CryptoCompareCurrency.USD), now.getEpochSecond()))
                .thenThrow(new Exception("Historical API not available."));

        ratesProviderService.getRate(CurrencyType.ETH, now);
    }

    private ExchangeAggregateRate createExchangeAggregateRate(Date creationDate) {
        return new ExchangeAggregateRate(creationDate, null, null);
    }

}
