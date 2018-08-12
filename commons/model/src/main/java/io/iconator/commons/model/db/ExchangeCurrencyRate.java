package io.iconator.commons.model.db;

import io.iconator.commons.model.CurrencyType;

import javax.persistence.*;
import java.math.BigDecimal;

import static javax.persistence.GenerationType.SEQUENCE;

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
