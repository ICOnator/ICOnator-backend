package io.iconator.commons.model.db;

import io.iconator.commons.model.ExchangeType;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.SEQUENCE;
import static javax.persistence.TemporalType.TIMESTAMP;

@Entity
@Table(name = "exchange_entry_rate")
public class ExchangeEntryRate {

    @Id
    @GeneratedValue(strategy = SEQUENCE)
    @Column(name = "id", nullable = false)
    private long id;

    @Temporal(TIMESTAMP)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "exchange_type", nullable = false)
    private ExchangeType exchangeType;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "exchange_currency_rates_id")
    private Set<ExchangeCurrencyRate> exchangeCurrencyRates = new HashSet<>();

    public ExchangeEntryRate() {
    }

    public ExchangeEntryRate(Date creationDate, ExchangeType exchangeType) {
        this.creationDate = creationDate;
        this.exchangeType = exchangeType;
    }

    public long getId() {
        return id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public Set<ExchangeCurrencyRate> getExchangeCurrencyRates() {
        return exchangeCurrencyRates;
    }

    public void addCurrencyRate(ExchangeCurrencyRate exchangeCurrencyRate) {
        this.exchangeCurrencyRates.add(exchangeCurrencyRate);
    }

}
