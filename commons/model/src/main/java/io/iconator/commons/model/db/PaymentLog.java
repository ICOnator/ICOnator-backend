package io.iconator.commons.model.db;

import com.sun.istack.internal.NotNull;
import io.iconator.commons.model.CurrencyType;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
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

    @Column(name = "block_date")
    private Date blockDate;

    @Column(name = "currency", nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrencyType currency;

    @Column(name = "payment_amount", precision = 34, scale = 0)
    private BigInteger paymentAmount;

    @Column(name = "fx_rate")
    private BigDecimal usdFxRate;

    @Column(name = "usd_amount", precision = 34, scale = 6)
    private BigDecimal usdValue;

    @Column(name = "investor_id")
    private long investorId;

    @Column(name = "tomics_amount", precision = 34, scale = 0)
    private BigInteger tomicsAmount;

    @Column(name = "eligible_for_refund_id")
    private Long eligibleForRefundId;

    public PaymentLog(@NotNull String txIdentifier, @NotNull Date createDate,
                      @NotNull CurrencyType currency) {
        this.txIdentifier = txIdentifier;
        this.createDate = createDate;
        this.currency = currency;
    }

    public PaymentLog(@NotNull String txIdentifier, @NotNull Date createDate,
                      @NotNull CurrencyType currency, Date blockDate,
                      BigInteger paymentAmount, BigDecimal usdFxRate, BigDecimal usdValue,
                      long investorId, BigInteger tomicsAmount) {
        this.txIdentifier = txIdentifier;
        this.createDate = createDate;
        this.blockDate = blockDate;
        this.currency = currency;
        this.paymentAmount = paymentAmount;
        this.usdFxRate = usdFxRate;
        this.usdValue = usdValue;
        this.investorId = investorId;
        this.tomicsAmount = tomicsAmount;
    }

    public String getTxIdentifier() {
        return txIdentifier;
    }

    public void setTxIdentifier(String txIdentifier) {
        this.txIdentifier = txIdentifier;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getBlockDate() {
        return blockDate;
    }

    public void setBlockDate(Date blockDate) {
        this.blockDate = blockDate;
    }

    public CurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyType currency) {
        this.currency = currency;
    }

    public BigInteger getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigInteger paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public BigDecimal getUsdFxRate() {
        return usdFxRate;
    }

    public void setUsdFxRate(BigDecimal usdFxRate) {
        this.usdFxRate = usdFxRate;
    }

    public BigDecimal getUsdValue() {
        return usdValue;
    }

    public void setUsdValue(BigDecimal usdValue) {
        this.usdValue = usdValue;
    }

    public long getInvestorId() {
        return investorId;
    }

    public void setInvestorId(long investorId) {
        this.investorId = investorId;
    }

    public BigInteger getTomicsAmount() {
        return tomicsAmount;
    }

    public void setTomicsAmount(BigInteger tomicsAmount) {
        this.tomicsAmount = tomicsAmount;
    }

    public Long getEligibleForRefundId() {
        return eligibleForRefundId;
    }

    public void setEligibleForRefundId(Long eligibleForRefundId) {
        this.eligibleForRefundId = eligibleForRefundId;
    }

    /**
     * A payment is completely processed if tokens have been allocated or a
     * refund entry has been created because of some inconsistencies with the
     * transaction. If none the two is seen on this PaymentLog then the
     * processing of the payment was probably interrupted by an unexpected stop
     * of the monitor application.
     * @return true if this PaymentLog has the tooken amount or a refund entry
     * reference set (or both). False otherwise.
     */
    public boolean wasFullyProcessed() {
        boolean hasTomicsSet = getTomicsAmount() != null
                && getTomicsAmount().compareTo(BigInteger.ZERO) > 0;
        boolean hasRefundEntry = getEligibleForRefundId() != null
                && getEligibleForRefundId() > 0;
        return hasTomicsSet || hasRefundEntry;
    }

    public boolean wasCreatedRecently(long timeSpanInMs) {
        long now = new Date().getTime();
        return now - getCreateDate().getTime() < timeSpanInMs;
    }
}
