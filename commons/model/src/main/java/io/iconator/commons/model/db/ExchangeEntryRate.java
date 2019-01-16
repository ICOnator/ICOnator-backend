package io.iconator.commons.model.db;

import io.iconator.commons.model.ExchangeType;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.GenerationType.SEQUENCE;
import static javax.persistence.TemporalType.TIMESTAMP;

/**
 * An instance of this entity represents a record of multiple exchange rates for cryptocurrencies
 * taken at a specific time from a specific exchange service. It references mutliple
 * {@link ExchangeCurrencyRate}s (one for each cryptocurrency) which contain the actual exchange
 * rate values.
 */
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

    /**
     * The exchange service used to retrieve the exchange rates.
     */
    @Column(name = "exchange_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ExchangeType exchangeType;

    /**
     * The set of exchange rates for the different cryptocurrencies.
     */
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
