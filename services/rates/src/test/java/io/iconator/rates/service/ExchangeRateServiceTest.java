package io.iconator.rates.service;

import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.ExchangeType;
import io.iconator.commons.model.db.ExchangeAggregateRate;
import io.iconator.commons.sql.dao.ExchangeAggregateRateRepository;
import io.iconator.rates.config.AggregationServiceConfig;
import io.iconator.rates.config.BlockNrServiceConfig;
import io.iconator.rates.config.ExchangeRateServiceConfig;
import io.iconator.rates.config.RatesAppConfig;
import io.iconator.rates.config.RatesAppConfigHolder;
import io.iconator.rates.config.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        TestConfig.class,
        AggregationServiceConfig.class,
        ExchangeRateServiceConfig.class,
        RatesAppConfigHolder.class,
        BlockNrServiceConfig.class,
        RatesAppConfig.class,
        BlockchainInfoService.class,
        EtherscanService.class,
        BlockNrProviderService.class
})
@DataJpaTest
@TestPropertySource({"classpath:rates.application.properties", "classpath:application-test.properties"})
public class ExchangeRateServiceTest {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private ExchangeAggregateRateRepository exchangeAggregateRateRepository;

    @Test
    public void testFetchRates() {
        exchangeRateService.fetchRates();
        List<ExchangeAggregateRate> all = exchangeAggregateRateRepository.findAllByOrderByCreationDate();
        assertTrue(all.stream().anyMatch((aggregateRate) -> aggregateRate.getExchangeEntryRates().size() > 0));
        assertTrue(checkIfExchangeIsPresent(all, ExchangeType.BITFINEX));
        assertTrue(checkIfExchangeIsPresent(all, ExchangeType.BITSTAMP));
        assertTrue(checkIfExchangeIsPresent(all, ExchangeType.KRAKEN));
        assertTrue(checkIfExchangeIsPresent(all, ExchangeType.GDAX));
        assertTrue(checkIfExchangeIsPresent(all, ExchangeType.COINMARKETCAP));
        assertTrue(checkIfCurrencyIsPresent(all, CurrencyType.ETH));
        assertTrue(checkIfCurrencyIsPresent(all, CurrencyType.BTC));
    }

    private boolean checkIfCurrencyIsPresent(List<ExchangeAggregateRate> rates, CurrencyType currencyType) {
        return rates.stream().anyMatch((aggregateRate) -> {
            return aggregateRate.getExchangeEntryRates().stream().anyMatch((entry) -> {
                return entry.getExchangeCurrencyRates().stream().anyMatch((currency) -> {
                    return currency.getCurrencyType() == currencyType;
                });
            });
        });
    }

    private boolean checkIfExchangeIsPresent(List<ExchangeAggregateRate> rates, ExchangeType exchangeType) {
        return rates.stream().anyMatch((aggregateRate) -> {
            return aggregateRate.getExchangeEntryRates().stream().anyMatch((entry) -> {
                return entry.getExchangeType() == exchangeType;
            });
        });
    }

}
