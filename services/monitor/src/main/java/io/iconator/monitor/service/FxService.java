package io.iconator.monitor.service;

import io.iconator.commons.amqp.model.FetchRatesRequestMessage;
import io.iconator.commons.amqp.model.FetchRatesResponseMessage;
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.amqp.service.exceptions.InvalidMessageFormatException;
import io.iconator.commons.model.CurrencyType;
import io.iconator.monitor.service.exceptions.FxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

@Service
public class FxService {

    @Autowired
    private ICOnatorMessageService messageService;

    /**
     * @param blockTimestamp the block's timestamp for which the exchange rate shall be fetched.
     * @param currencyType   the crypto currency for which the exchange rate shall be fetched.
     * @return an optional object containing the exchange rate in USD per crypto currency unit.
     * @throws FxException if the exchange rate could could not be fetched.
     */
    public Optional<BigDecimal> getUSDExchangeRate(Instant blockTimestamp, CurrencyType currencyType)
            throws FxException {
        Optional<BigDecimal> result;
        try {
            FetchRatesResponseMessage responseMessage = messageService.sendAndReceive(
                    new FetchRatesRequestMessage(Date.from(blockTimestamp),
                            CurrencyType.USD, Arrays.asList(currencyType)));

            result = responseMessage.getExchangeAggregateRates().stream()
                    .filter((aggregateRate) -> aggregateRate.getCurrencyType().equals(currencyType))
                    .findFirst()
                    .map((dtoObj) -> dtoObj.getAggregateExchangeRate());
        } catch (InvalidMessageFormatException e) {
            throw new FxException("Not possible to fetch rate.", e);
        }
        return result;
    }

}
