package io.iconator.commons.model.db;

import io.iconator.commons.model.CurrencyType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * An instance of this entity represents a record of an aggregated exchange rate at a specific time
 * for a specific cryptocurrency. The timestamp at which the rate was retrieved and aggregated is
 * given in the corresponding {@link ExchangeAggregateRate}. The aggregation happens over exchange
 * rates taken from multiple exchange services.
 * The rate is meant to be fiat base currency (e.g. USD) per cruptocurrency unit.
 */
@Entity
@Table(name = "exchange_aggregate_currency_rate")
public class ExchangeAggregateCurrencyRate {

    @Id
    @GeneratedValue(strategy = SEQUENCE)
    @Column(name = "id", nullable = false)
    private long id;

    /**
     * The cryptocurrency for which this is the exchange rate.
     */
    @Column(name = "currency_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrencyType currencyType;

    /**
     * The exchange rate value aggregated over rates taken from multiple sources.
     */
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
