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

/**
 * Represents an investor's eligibility for a refund. These refund entries are created if a payment
 * of an investor cannot be processed correctly. The reasons for which a refund entry is created
 * are given in the {@link RefundReason} enum.
 */
@Entity
@Table(name = "eligible_for_refund")
public class EligibleForRefund {

    public enum RefundReason {
        INVESTOR_MISSING,
        TOKEN_OVERFLOW,
        FX_RATE_MISSING,
        TOKEN_ALLOCATION_FAILED,
        BLOCK_TIME_MISSING,
        TRANSACTION_VALUE_MISSING,
        INSUFFICIENT_PAYMENT_AMOUNT
    }

    @Id
    @GeneratedValue(strategy = SEQUENCE)
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "refund_reason")
    @Enumerated(EnumType.STRING)
    private RefundReason refundReason;

    /**
     * The amount in cryptocurrency that this refund is worth.
     */
    @Column(name = "cryptocurrencyAmount", precision = 34, scale = 0)
    private BigInteger cryptocurrencyAmount;

    /**
     * The amount in fiat currency (USD) that this refund is worth.
     */
    @Column(name = "usd_amount", precision = 34, scale = 6)
    private BigDecimal usdAmount;

    @Column(name = "cryptocurrency_type")
    @Enumerated(EnumType.STRING)
    private CurrencyType currency;

    /**
     * The investor that is eligible for this refund.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investor_id", nullable = false)
    private Investor investor;

    /**
     * The transaction for which this refund entry was created.
     */
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
