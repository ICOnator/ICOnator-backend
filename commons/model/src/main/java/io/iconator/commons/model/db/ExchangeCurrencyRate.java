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
 * An instance of this entity represents a record of an exchange rate at a specific time for a
 * specific cryptocurrency. The timestamp at which the rate was retrieved is given in the
 * corresponding {@link ExchangeEntryRate}.
 * The rate is meant to be fiat base currency (e.g. USD) per cruptocurrency unit.
 */
@Entity
@Table(name = "exchange_currency_rate")
public class ExchangeCurrencyRate {

    @Id
    @GeneratedValue(strategy = SEQUENCE)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "currency_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrencyType currencyType;

    @Column(name = "exchange_rate", precision = 18, scale = 6)
    private BigDecimal exchangeRate;

    public ExchangeCurrencyRate() {
    }

    public ExchangeCurrencyRate(CurrencyType currencyType, BigDecimal exchangeRate) {
        this.currencyType = currencyType;
        this.exchangeRate = exchangeRate;
    }

    public long getId() {
        return id;
    }

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }
}
