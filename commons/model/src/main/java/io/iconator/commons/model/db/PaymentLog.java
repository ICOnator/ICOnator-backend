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
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity(name = "payment_log")
public class PaymentLog {

    public enum TransactionStatus {
        PENDING,
        BUILDING,
        CONFIRMED
    }

    @Version
    @Column(name = "version")
    private Long version = 0L;

    @Id
    @GeneratedValue(strategy = SEQUENCE)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_date", nullable = false)
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "processed_date")
    private Date processedDate = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "block_time")
    private Date blockTime;

    @Column(name = "cryptocurrency_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrencyType currency;

    @Column(name = "cryptocurrency_amount", precision = 34, scale = 0)
    private BigInteger cryptocurrencyAmount = BigInteger.ZERO;

    @Column(name = "usd_fx_rate")
    private BigDecimal usdFxRate = BigDecimal.ZERO;

    @Column(name = "usd_amount", precision = 34, scale = 6)
    private BigDecimal usdAmount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "investor_id")
    private Investor investor;

    @Column(name = "allocated_tomics", precision = 34, scale = 0)
    private BigInteger allocatedTomics = null;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eligible_for_refund_id")
    private EligibleForRefund eligibleForRefund;

    @Column(name = "is_transaction_received_message_sent")
    private Boolean isTransactionReceivedMessageSent = false;

    @Column(name = "is_allocation_message_sent")
    private Boolean isAllocationMessageSent = false;

    @Column(name = "transaction_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    public PaymentLog() {
    }

    public PaymentLog(String transactionId, Date createDate,
                      CurrencyType currency, TransactionStatus transactionStatus) {
        this.transactionId = transactionId;
        this.createDate = createDate;
        this.currency = currency;
        this.transactionStatus = transactionStatus;
    }

    public PaymentLog(String transactionId, Date createDate,
                      CurrencyType currency, Date blockTime,
                      BigInteger cryptocurrencyAmount, BigDecimal usdFxRate,
                      BigDecimal usdAmount, Investor investor, BigInteger allocatedTomics,
                      TransactionStatus transactionStatus) {
        this.transactionId = transactionId;
        this.createDate = createDate;
        this.blockTime = blockTime;
        this.currency = currency;
        this.cryptocurrencyAmount = cryptocurrencyAmount;
        this.usdFxRate = usdFxRate;
        this.usdAmount = usdAmount;
        this.investor = investor;
        this.allocatedTomics = allocatedTomics;
        this.transactionStatus = transactionStatus;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(Date blockTime) {
        this.blockTime = blockTime;
    }

    public CurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyType currency) {
        this.currency = currency;
    }

    public BigInteger getCryptocurrencyAmount() {
        return cryptocurrencyAmount;
    }

    public void setCryptocurrencyAmount(BigInteger cryptocurrencyAmount) {
        this.cryptocurrencyAmount = cryptocurrencyAmount;
    }

    public BigDecimal getUsdFxRate() {
        return usdFxRate;
    }

    public void setUsdFxRate(BigDecimal usdFxRate) {
        this.usdFxRate = usdFxRate;
    }

    public BigDecimal getUsdAmount() {
        return usdAmount;
    }

    public void setUsdAmount(BigDecimal usdAmount) {
        this.usdAmount = usdAmount;
    }

    public Investor getInvestor() {
        return investor;
    }

    public void setInvestor(Investor investor) {
        this.investor = investor;
    }

    public BigInteger getAllocatedTomics() {
        return allocatedTomics;
    }

    public void setAllocatedTomics(BigInteger allocatedTomics) {
        this.allocatedTomics = allocatedTomics;
    }

    public EligibleForRefund getEligibleForRefund() {
        return eligibleForRefund;
    }

    public void setEligibleForRefund(EligibleForRefund eligibleForRefund) {
        this.eligibleForRefund = eligibleForRefund;
    }

    public boolean isTransactionReceivedMessageSent() {
        return isTransactionReceivedMessageSent;
    }

    public void setTransactionReceivedMessageSent(Boolean transactionReceivedMessageSent) {
        isTransactionReceivedMessageSent = transactionReceivedMessageSent;
    }

    public boolean isAllocationMessageSent() {
        return isAllocationMessageSent;
    }

    public void setAllocationMessageSent(Boolean allocationMessageSent) {
        isAllocationMessageSent = allocationMessageSent;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public Date getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(Date processedDate) {
        this.processedDate = processedDate;
    }
}
