package io.iconator.commons.model.db;

import io.iconator.commons.model.ExchangeType;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.TemporalType.TIMESTAMP;

@Entity
@Table(name = "exchange_entry_rate")
public class ExchangeEntryRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Temporal(TIMESTAMP)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_aggregate_rate_id")
    private ExchangeAggregateRate exchangeAggregateRate;

    @Column(name = "exchange_type", nullable = false)
    private ExchangeType exchangeType;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "exchangeEntryRate")
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

    public ExchangeAggregateRate getExchangeAggregateRate() {
        return exchangeAggregateRate;
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
