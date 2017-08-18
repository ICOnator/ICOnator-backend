package io.modum.tokenapp.rates.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import static javax.persistence.TemporalType.TIMESTAMP;


@Entity
@Table(name = "exchange_rate", indexes = {
        @Index(columnList = "block_nr_eth", name = "block_nr_eth_idx"),
        @Index(columnList = "block_nr_btc", name = "block_nr_btc_idx")})
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Temporal(TIMESTAMP)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Column(name = "rate_eth")
    private BigDecimal rateEth;

    @Column(name = "rate_btc")
    private BigDecimal rateBtc;

    @Column(name = "rate_eth_bitfinex")
    private BigDecimal rateEthBitfinex;

    @Column(name = "rate_btc_bitfinex")
    private BigDecimal rateBtcBitfinex;

    @Column(name = "rate_iota_bitfinex")
    private BigDecimal rateIotaBitfinex;

    @Column(name = "block_nr_eth")
    private Long blockNrEth;

    @Column(name = "block_nr_btc")
    private Long blockNrBtc;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public ExchangeRate setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public BigDecimal getRateBtcBitfinex() {
        return rateBtcBitfinex;
    }

    public ExchangeRate setRateBtcBitfinex(BigDecimal rateBtcBitfinex) {
        this.rateBtcBitfinex = rateBtcBitfinex;
        return this;
    }

    public BigDecimal getRateIotaBitfinex() {
        return rateIotaBitfinex;
    }

    public ExchangeRate setRateIotaBitfinex(BigDecimal rateIotaBitfinex) {
        this.rateIotaBitfinex = rateIotaBitfinex;
        return this;
    }

    public BigDecimal getRateEthBitfinex() {
        return rateEthBitfinex;
    }

    public ExchangeRate setRateEthBitfinex(BigDecimal rateEthBitfinex) {
        this.rateEthBitfinex = rateEthBitfinex;
        return this;
    }

    public BigDecimal getRateBtc() {
        return rateBtc;
    }

    public ExchangeRate setRateBtc(BigDecimal rateBtc) {
        this.rateBtc = rateBtc;
        return this;
    }

    public BigDecimal getRateEth() {
        return rateEth;
    }

    public ExchangeRate setRateEth(BigDecimal rateEth) {
        this.rateEth = rateEth;
        return this;
    }

    public Long getBlockNrBtc() {
        return blockNrBtc;
    }

    public ExchangeRate setBlockNrBtc(Long blockNrBtc) {
        this.blockNrBtc = blockNrBtc;
        return this;
    }

    public Long getBlockNrEth() {
        return blockNrEth;
    }

    public ExchangeRate setBlockNrEth(Long blockNrEth) {
        this.blockNrEth = blockNrEth;
        return this;
    }
}
