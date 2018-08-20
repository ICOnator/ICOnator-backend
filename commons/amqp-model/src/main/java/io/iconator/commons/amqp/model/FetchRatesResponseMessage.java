package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.iconator.commons.amqp.model.dto.ExchangeAggregateCurrencyRateDTO;
import io.iconator.commons.model.CurrencyType;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchRatesResponseMessage extends Message {

    @JsonProperty("from")
    private CurrencyType from;

    @JsonProperty("exchangeAggregateRates")
    private List<ExchangeAggregateCurrencyRateDTO> exchangeAggregateRates;

    public FetchRatesResponseMessage() {
        super();
    }

    public FetchRatesResponseMessage(CurrencyType from, List<ExchangeAggregateCurrencyRateDTO> exchangeAggregateRates) {
        super();
        this.from = from;
        this.exchangeAggregateRates = exchangeAggregateRates;
    }

    public CurrencyType getFrom() {
        return from;
    }

    public List<ExchangeAggregateCurrencyRateDTO> getExchangeAggregateRates() {
        return exchangeAggregateRates;
    }
}
