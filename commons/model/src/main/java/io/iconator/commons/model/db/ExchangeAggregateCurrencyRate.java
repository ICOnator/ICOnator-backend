package io.iconator.commons.model.db;

import io.iconator.commons.model.CurrencyType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "exchange_aggregate_currency_rate")
public class ExchangeAggregateCurrencyRate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_aggregate_rate_id")
    private ExchangeAggregateRate exchangeAggregateRate;

    @Column(name = "currency_type", nullable = false)
    private CurrencyType currencyType;

    @Column(name = "aggregate_exchange_rate")
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

    public ExchangeAggregateRate getExchangeAggregateRate() {
        return exchangeAggregateRate;
    }

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    public BigDecimal getAggregateExchangeRate() {
        return aggregateExchangeRate;
    }
}
