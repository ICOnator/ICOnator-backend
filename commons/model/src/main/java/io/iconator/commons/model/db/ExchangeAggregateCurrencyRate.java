package io.iconator.commons.model.db;

import io.iconator.commons.model.CurrencyType;

import javax.persistence.*;
import java.math.BigDecimal;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "exchange_aggregate_currency_rate")
public class ExchangeAggregateCurrencyRate {

    @Id
    @GeneratedValue(strategy = SEQUENCE)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "currency_type", nullable = false)
    private CurrencyType currencyType;

    @Column(name = "aggregate_exchange_rate", precision = 18, scale = 6)
    private BigDecimal aggregateExchangeRate;

    public ExchangeAggregateCurrencyRate() {
    }

    public ExchangeAggregateCurrencyRate(CurrencyType currencyType, BigDecimal aggregateExchangeRate) {
        this.currencyType = currencyType;
        this.aggregateExchangeRate = aggregateExchangeRate;
    }

    public long getId() {
        return id;
    }

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    public BigDecimal getAggregateExchangeRate() {
        return aggregateExchangeRate;
    }
}
