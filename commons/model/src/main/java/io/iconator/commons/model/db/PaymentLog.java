package io.iconator.commons.model.db;

import io.iconator.commons.model.CurrencyType;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity(name = "payment_log")
public class PaymentLog {

    @Id
    @GeneratedValue(strategy = SEQUENCE)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "tx_identifier", unique = true, nullable = false)
    private String txIdentifier;

    @Column(name = "create_date", nullable = false)
    private Date createDate;

    @Column(name = "block_date", nullable = false)
    private Date blockDate;

    @Column(name = "currency", nullable = false)
    private CurrencyType currency;

    @Column(name = "payment_amount", nullable = false)
    private BigDecimal paymentAmount;

    @Column(name = "fx_rate", nullable = false)
    private BigDecimal fxRate;

    @Column(name = "usd_amount", nullable = false)
    private BigDecimal usdValue;

    @OneToOne()
    @JoinColumn(name = "investor", nullable = false)
    private Investor investor;

    @Column(name = "token_amount", nullable = false)
    private BigDecimal tokenAmount;

    public PaymentLog() {
    }

    public PaymentLog(String txIdentifier, Date createDate, Date blockDate, CurrencyType currency,
                      BigDecimal paymentAmount, BigDecimal fxRate, BigDecimal usdValue,
                      Investor investor, BigDecimal tokenAmount) {
        this.txIdentifier = txIdentifier;
        this.createDate = createDate;
        this.blockDate = blockDate;
        this.currency = currency;
        this.paymentAmount = paymentAmount;
        this.fxRate = fxRate;
        this.usdValue = usdValue;
        this.investor = investor;
        this.tokenAmount = tokenAmount;
    }

    public long getId() {
        return id;
    }

    public String getTxIdentifier() {
        return txIdentifier;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public Date getBlockDate() {
        return blockDate;
    }

    public CurrencyType getCurrency() {
        return currency;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public BigDecimal getFxRate() {
        return fxRate;
    }

    public BigDecimal getUsdValue() {
        return usdValue;
    }

    public BigDecimal getTokenAmount() {
        return tokenAmount;
    }

    public void setTokenAmount(BigDecimal amount) {
        this.tokenAmount = amount;
    }

    public Investor getInvestor() {
        return investor;
    }
}
