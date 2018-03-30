package io.iconator.commons.model.db;

import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.ExchangeType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import java.math.BigDecimal;
import java.util.Date;

import static javax.persistence.TemporalType.TIMESTAMP;

@Entity
@Table(name = "exchange_currency_rate")
public class ExchangeCurrencyRate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_entry_rate_id")
    private ExchangeEntryRate exchangeEntryRate;

    @Column(name = "exchange_type", nullable = false)
    private CurrencyType currencyType;

    @Column(name = "exchange_rate")
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

    public ExchangeEntryRate getExchangeEntryRate() {
        return exchangeEntryRate;
    }

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }
}
