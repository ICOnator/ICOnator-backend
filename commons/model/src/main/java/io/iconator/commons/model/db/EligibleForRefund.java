package io.iconator.commons.model.db;

import io.iconator.commons.model.CurrencyType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.math.BigInteger;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "eligible_for_refund")
public class EligibleForRefund {

    public enum RefundReason {
        INVESTOR_MISSING,
        TOKEN_OVERFLOW,
        FX_RATE_MISSING,
        TOKEN_ALLOCATION_FAILED,
        BLOCK_TIME_MISSING,
        BLOCK_HEIGHT_MISSING,
        TRANSACTION_VALUE_MISSING
    }

    @Id
    @GeneratedValue(strategy = SEQUENCE)
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "refund_reason")
    @Enumerated(EnumType.STRING)
    private RefundReason refundReason;

    @Column(name = "cryptocurrencyAmount", precision = 34, scale = 0)
    private BigInteger cryptocurrencyAmount;

    @Column(name = "usd_amount", precision = 34, scale = 6)
    private BigDecimal usdAmount;

    @Column(name = "currency")
    @Enumerated(EnumType.STRING)
    private CurrencyType currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investor_id", nullable = false)
    private Investor investor;

    @Column(name = "tx_identifier", unique = true, nullable = false)
    private String txIdentifier;

    public EligibleForRefund() {
    }

    public EligibleForRefund(RefundReason refundReason, BigInteger cryptocurrencyAmount, BigDecimal usdAmount, CurrencyType currency,
                             Investor investor, String txIdentifier) {
        this.refundReason = refundReason;
        this.cryptocurrencyAmount = cryptocurrencyAmount;
        this.usdAmount = usdAmount;
        this.currency = currency;
        this.investor = investor;
        this.txIdentifier = txIdentifier;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public RefundReason getRefundReason() {
        return refundReason;
    }

    public void setRefundReason(RefundReason refundReason) {
        this.refundReason = refundReason;
    }

    public BigInteger getCryptocurrencyAmount() {
        return cryptocurrencyAmount;
    }

    public void setCryptocurrencyAmount(BigInteger cryptocurrencyAmount) {
        this.cryptocurrencyAmount = cryptocurrencyAmount;
    }

    public CurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyType currency) {
        this.currency = currency;
    }

    public Investor getInvestor() {
        return investor;
    }

    public void setInvestor(Investor investor) {
        this.investor = investor;
    }

    public String getTxIdentifier() {
        return txIdentifier;
    }

    public void setTxIdentifier(String txIdentifier) {
        this.txIdentifier = txIdentifier;
    }

    public BigDecimal getUsdAmount() {
        return usdAmount;
    }

    public void setUsdAmount(BigDecimal usdAmount) {
        this.usdAmount = usdAmount;
    }
}
