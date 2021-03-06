package io.iconator.commons.model.db;

import io.iconator.commons.model.CurrencyType;
import org.springframework.lang.NonNull;

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

/**
 * A payment log holds information about a payment received from an investor. It holds the required
 * information for processing the corresponding payment. It is also used to track the state of the
 * processing, e.g. if the payment was fully processed and can be considered completed.
 * The terms payment and transaction are used as synonymes.
 */
@Entity(name = "payment_log")
public class PaymentLog {

    /**
     * A payment can be in either of the three status given in this enum.
     * These status correspond to the states of a transaction on the blockchain.
     */
    public enum TransactionStatus {
        PENDING, /* The transaction is in the network but not yet on a block. */
        BUILDING, /* The trannsaction is on a block. */
        CONFIRMED /* The transaction is covered by enough blocks to be considered confirmed. */
    }

    /**
     * The version number is needed for optimistic locking in concurrent database accesses.
     */
    @Version
    @Column(name = "version")
    private Long version = 0L;

    @Id
    @GeneratedValue(strategy = SEQUENCE)
    @Column(name = "id", nullable = false)
    private long id;

    /**
     * The id of the blockchain transaction that this payment log corresponds to.
     */
    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_date", nullable = false)
    private Date createDate = new Date();

    /**
     * The date and time on which this payment log was last processed.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "processed_date")
    private Date processedDate = new Date();

    /**
     * The time at which the transaction was added to a block.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "block_time")
    private Date blockTime;

    /**
     * The cryptocurrency in which the payment was made.
     */
    @Column(name = "cryptocurrency_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrencyType currency;

    /**
     * The payment amount in cryptocurrency.
     */
    @Column(name = "cryptocurrency_amount", precision = 34, scale = 0)
    private BigInteger cryptocurrencyAmount = BigInteger.ZERO;

    /**
     * The exchange rate used to convert from the cryptocurrency value of this payment to fiat
     * currency value (USD).
     */
    @Column(name = "usd_fx_rate")
    private BigDecimal usdFxRate = BigDecimal.ZERO;

    /**
     * The payment amount in USD.
     */
    @Column(name = "usd_amount", precision = 34, scale = 6)
    private BigDecimal usdAmount = BigDecimal.ZERO;

    /**
     * The investor that made this payment.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "investor_id")
    private Investor investor;

    /**
     * The amount of tokens that were allocated for this payment. Given in the token's atomic unit.
     *
     * Note: It is important that the default value is null and not 0 because this is
     * used for checks in the processing of transactions.
     */
    @Column(name = "allocated_tomics", precision = 34, scale = 0)
    private BigInteger allocatedTomics = null;

    /**
     * A reference to a refund entry if the processing of the payment ran into an error.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eligible_for_refund_id")
    private EligibleForRefund eligibleForRefund;

    /**
     * This flag is true if a message was sent to the investor that informs him that his transaction
     * was seen on the blockchain network.
     */
    @Column(name = "is_transaction_received_message_sent")
    private Boolean isTransactionReceivedMessageSent = false;

    /**
     * This flag is true if a message was sent to the investor that informs him about how many
     * tokens where allocated to him.
     */
    @Column(name = "is_allocation_message_sent")
    private Boolean isAllocationMessageSent = false;

    @Column(name = "transaction_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    public PaymentLog() {
    }

    public PaymentLog(String transactionId, CurrencyType currency,
                      TransactionStatus transactionStatus) {
        this.transactionId = transactionId;
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
