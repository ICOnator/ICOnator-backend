package io.iconator.commons.sql.dao;

import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.ExchangeType;
import io.iconator.commons.model.db.ExchangeAggregateCurrencyRate;
import io.iconator.commons.model.db.ExchangeAggregateRate;
import io.iconator.commons.model.db.ExchangeCurrencyRate;
import io.iconator.commons.model.db.ExchangeEntryRate;
import io.iconator.commons.sql.dao.config.TestConfig;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@DataJpaTest
public class ExchangeAggregateRateRepositoryTest {

    @Autowired
    private ExchangeAggregateRateRepository aggregateRateRepository;

    @Test
    public void testFindBlockNrBtcGreaterThan_HavingMultipleExchanges() {

        createExchangeAggregateRateWithMultipleExchangeEntry(ExchangeType.BITSTAMP, ExchangeType.KRAKEN);

        Optional<ExchangeAggregateRate> optionalAggregateRate = aggregateRateRepository
                .findFirstOptionalByBlockNrBtcLessThanEqualOrderByBlockNrBtcDesc(new Long(1));

        assertTrue(optionalAggregateRate.filter((aggRate) ->
                        aggRate.getExchangeEntryRates().stream()
                                .anyMatch((exRate) -> exRate.getExchangeType() == ExchangeType.BITSTAMP)
                ).isPresent()
        );
        assertTrue(optionalAggregateRate.filter((aggRate) ->
                        aggRate.getExchangeEntryRates().stream()
                                .anyMatch((exRate) -> exRate.getExchangeType() == ExchangeType.KRAKEN)
                ).isPresent()
        );
        assertTrue(optionalAggregateRate.filter((aggRate) ->
                        aggRate.getExchangeAggregateCurrencyRates().stream()
                                .anyMatch((aggregateCurrencyRate) -> aggregateCurrencyRate.getCurrencyType() == CurrencyType.BTC)
                ).isPresent()
        );
        assertTrue(optionalAggregateRate.filter((aggRate) ->
                        aggRate.getExchangeAggregateCurrencyRates().stream()
                                .anyMatch((aggregateCurrencyRate) -> aggregateCurrencyRate.getCurrencyType() == CurrencyType.ETH)
                ).isPresent()
        );
    }

    @Test
    public void testFindBlockNrBtcGreaterThan_Equals() {

        createExchangeAggregateRateWithMultipleExchangeEntry(ExchangeType.BITSTAMP);

        Optional<ExchangeAggregateRate> optionalAggregateRate = aggregateRateRepository
                .findFirstOptionalByBlockNrBtcLessThanEqualOrderByBlockNrBtcDesc(new Long(1));

        assertTrue(optionalAggregateRate.isPresent());
        assertTrue(optionalAggregateRate.filter((aggRate) -> aggRate.getBlockNrBtc() == 1).isPresent());
    }

    @Test
    public void testFindBlockNrBtcLesserThan_NonExisting() {

        createExchangeAggregateRateWithMultipleExchangeEntry(ExchangeType.BITSTAMP);

        Optional<ExchangeAggregateRate> optionalAggregateRate = aggregateRateRepository
                .findFirstOptionalByBlockNrBtcLessThanEqualOrderByBlockNrBtcDesc(new Long(0));

        assertTrue(!optionalAggregateRate.isPresent());

    }

    @Test
    public void testFindBlockNrBtcByOrderDesc() {

        createMultipleExchangeAggregateRate();

        Optional<ExchangeAggregateRate> optionalAggregateRate = aggregateRateRepository
                .findFirstOptionalByOrderByBlockNrBtcDesc();

        assertTrue(optionalAggregateRate.isPresent());
        assertTrue(optionalAggregateRate.filter((aggRate) -> aggRate.getBlockNrBtc() == 3).isPresent());

    }

    @Test
    public void testFindAllByOrderDesc() {

        createMultipleExchangeAggregateRate();

        List<ExchangeAggregateRate> aggregateRates = aggregateRateRepository
                .findAllByOrderByCreationDate();

        long count = aggregateRates.stream().count();

        assertTrue(!aggregateRates.isEmpty());
        assertTrue(aggregateRates.stream().findFirst().filter((aggRate) -> aggRate.getBlockNrBtc() == 1).isPresent());
        assertTrue(aggregateRates.stream().skip(count - 1).findFirst().filter((aggRate) -> aggRate.getBlockNrBtc() == 3).isPresent());

    }



    private ExchangeAggregateRate createExchangeAggregateRateWithMultipleExchangeEntry(ExchangeType... exchangeTypes) {
        ExchangeAggregateRate aggregateRate = createExchangeAggregateRate();
        Arrays.asList(exchangeTypes).stream().forEach((type) -> {
            ExchangeEntryRate entryRate = createExchangeEntryRate(type);
            ExchangeCurrencyRate rateBTC = createExchangeCurrencyRate(CurrencyType.BTC);
            ExchangeCurrencyRate rateETH = createExchangeCurrencyRate(CurrencyType.ETH);
            entryRate.addCurrencyRate(rateBTC);
            entryRate.addCurrencyRate(rateETH);
            aggregateRate.addExchangeEntry(entryRate);
        });
        ExchangeAggregateCurrencyRate aggregateCurrencyRateBTC = createExchangeAggregateCurrencyRate(
                CurrencyType.BTC, aggregateRate.getAllExchangeCurrencyRates(CurrencyType.BTC));
        ExchangeAggregateCurrencyRate aggregateCurrencyRateETH = createExchangeAggregateCurrencyRate(
                CurrencyType.ETH, aggregateRate.getAllExchangeCurrencyRates(CurrencyType.ETH));
        aggregateRate.addExchangeAggregateCurrencyRate(aggregateCurrencyRateBTC);
        aggregateRate.addExchangeAggregateCurrencyRate(aggregateCurrencyRateETH);

        aggregateRateRepository.save(aggregateRate);
        return aggregateRate;
    }

    private void createMultipleExchangeAggregateRate() {
        createMultipleExchangeAggregateRate(Instant.now());
    }

    private void createMultipleExchangeAggregateRate(Instant now) {
        ExchangeAggregateRate a1 = createExchangeAggregateRate(
                Date.from(now.minusSeconds(10)),
                new Long(1),
                new Long(1));
        aggregateRateRepository.save(a1);
        ExchangeAggregateRate a2 = createExchangeAggregateRate(
                Date.from(now),
                new Long(2),
                new Long(2));
        aggregateRateRepository.save(a2);
        ExchangeAggregateRate a3 = createExchangeAggregateRate(
                Date.from(now.plusSeconds(10)),
                new Long(3),
                new Long(3));
        aggregateRateRepository.save(a3);
    }

    private ExchangeAggregateRate createExchangeAggregateRate() {
        return createExchangeAggregateRate(new Date(), new Long(1), new Long(1));
    }

    private ExchangeAggregateRate createExchangeAggregateRate(Date creationDate, Long blockNrEth, Long blockNrBtc) {
        return new ExchangeAggregateRate(
                creationDate,
                blockNrEth,
                blockNrBtc
        );
    }

    private ExchangeEntryRate createExchangeEntryRate(ExchangeType exchangeType) {
        return new ExchangeEntryRate(
                new Date(),
                exchangeType
        );
    }

    private ExchangeCurrencyRate createExchangeCurrencyRate(CurrencyType currencyType) {
        return new ExchangeCurrencyRate(currencyType, new BigDecimal(123.45));
    }

    private ExchangeAggregateCurrencyRate createExchangeAggregateCurrencyRate(CurrencyType currencyType, List<ExchangeCurrencyRate> currencyRates) {
        Mean mean = new Mean();
        double[] rateDoubles = currencyRates.stream().mapToDouble((value) -> value.getExchangeRate().doubleValue()).toArray();
        double meanValue = mean.evaluate(rateDoubles, 0, rateDoubles.length);
        return new ExchangeAggregateCurrencyRate(currencyType, new BigDecimal(meanValue));
    }
}
