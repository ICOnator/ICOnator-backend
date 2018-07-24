package io.iconator.commons.model.db;

import io.iconator.commons.model.CurrencyType;

import javax.persistence.*;
import java.math.BigInteger;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "eligible_for_refund")
public class EligibleForRefund {

    public enum RefundReason {
        NO_INVESTOR_FOUND_FOR_RECEIVING_ADDRESS,
        FINAL_TIER_OVERFLOW,
        MISSING_FX_RATE,
        FAILED_CONVERSION_TO_TOKENS,
        FAILED_CONVERSION_TO_USD,
        FAILED_CONVERSION_FROM_WEI_TO_ETHER,
        FAILED_CONVERSION_FROM_SATOSHI_TO_COIN,
        MISSING_BLOCK_TIMESTAMP,
        MISSING_BLOCK_BTC_NR,
        FAILED_CREATING_PAYMENTLOG
    }

    @Id
    @GeneratedValue(strategy = SEQUENCE)
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "refund_reason")
    @Enumerated(EnumType.STRING)
    private RefundReason refundReason;

    @Column(name = "amount", precision = 34, scale = 0)
    private BigInteger amount;

    @Column(name = "currency")
    @Enumerated(EnumType.STRING)
    private CurrencyType currency;

    @Column(name = "inverstor_id")
    private long investorId;

    @Column(name = "tx_identifier", unique = true, nullable = false)
    private String txIdentifier;

    protected EligibleForRefund() {
    }

    public EligibleForRefund(RefundReason refundReason, BigInteger amount, CurrencyType currency,
                             long investorId, String txIdentifier) {
        this.refundReason = refundReason;
        this.amount = amount;
        this.currency = currency;
        this.investorId = investorId;
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

    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    public CurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyType currency) {
        this.currency = currency;
    }

    public long getInvestorId() {
        return investorId;
    }

    public void setInvestorId(long investorId) {
        this.investorId = investorId;
    }

    public String getTxIdentifier() {
        return txIdentifier;
    }

    public void setTxIdentifier(String txIdentifier) {
        this.txIdentifier = txIdentifier;
    }
}
