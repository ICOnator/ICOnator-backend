package io.iconator.commons.amqp.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.iconator.commons.model.CurrencyType;

import java.math.BigDecimal;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeAggregateCurrencyRateDTO {

    @JsonProperty("aggregateRateDate")
    private Date aggregateRateDate;

    @JsonProperty("currencyType")
    private CurrencyType currencyType;

    @JsonProperty("aggregateExchangeRate")
    private BigDecimal aggregateExchangeRate;

    public ExchangeAggregateCurrencyRateDTO() {
    }

    public ExchangeAggregateCurrencyRateDTO(Date aggregateRateDate, CurrencyType currencyType, BigDecimal aggregateExchangeRate) {
        this.aggregateRateDate = aggregateRateDate;
        this.currencyType = currencyType;
        this.aggregateExchangeRate = aggregateExchangeRate;
    }

    public Date getAggregateRateDate() {
        return aggregateRateDate;
    }

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    public BigDecimal getAggregateExchangeRate() {
        return aggregateExchangeRate;
    }

}
