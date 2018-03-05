package io.iconator.commons.model.db;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax.persistence.TemporalType.TIMESTAMP;

@Entity
@Table(name = "exchange_aggregate_rate", indexes = {
        @Index(columnList = "block_nr_eth", name = "block_nr_eth_idx"),
        @Index(columnList = "block_nr_btc", name = "block_nr_btc_idx")})
public class ExchangeAggregateRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Temporal(TIMESTAMP)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "block_nr_eth")
    private Long blockNrEth;

    @Column(name = "block_nr_btc")
    private Long blockNrBtc;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "exchangeAggregateRate")
    private List<ExchangeEntryRate> exchangeEntryRates = new ArrayList<>();

    public ExchangeAggregateRate() {
    }

    public ExchangeAggregateRate(Date creationDate, Long blockNrEth, Long blockNrBtc) {
        this.creationDate = creationDate;
        this.blockNrEth = blockNrEth;
        this.blockNrBtc = blockNrBtc;
    }

    public long getId() {
        return id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Long getBlockNrEth() {
        return blockNrEth;
    }

    public Long getBlockNrBtc() {
        return blockNrBtc;
    }

    public List<ExchangeEntryRate> getExchangeEntryRates() {
        return exchangeEntryRates;
    }

    public void addExchangeEntry(ExchangeEntryRate exchangeEntryRate) {
        exchangeEntryRates.add(exchangeEntryRate);
    }

}
