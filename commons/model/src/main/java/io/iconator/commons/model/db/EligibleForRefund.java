package io.iconator.commons.model.db;

import io.iconator.commons.model.CurrencyType;

import javax.persistence.*;
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

    @Column(name = "amount", precision = 34, scale = 0)
    private BigInteger amount;

    @Column(name = "currency")
    @Enumerated(EnumType.STRING)
    private CurrencyType currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investor_id", nullable = false)
    private Investor investor;

    @Column(name = "tx_identifier", unique = true, nullable = false)
    private String txIdentifier;

    public EligibleForRefund() {}

    public EligibleForRefund(RefundReason refundReason, BigInteger amount, CurrencyType currency,
                             Investor investor, String txIdentifier) {
        this.refundReason = refundReason;
        this.amount = amount;
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

//    public static class Builder {
//
//        private RefundReason refundReason;
//        private BigInteger amount;
//        private CurrencyType currency;
//        private long investorId;
//        private String txIdentifier;
//
//        public Builder() {}
//
//        public EligibleForRefund build() {
//            return new EligibleForRefund(this);
//        }
//
//        public Builder refundReason(RefundReason reason) {
//            if (reason == null) throw new NullPointerException("refund reason cannot be null.");
//            this.refundReason = reason;
//            return this;
//        }
//
//        public Builder amount(BigInteger amount) {
//            if (amount == null) throw new NullPointerException("amount cannot be null.");
//            this.amount = amount;
//            return this;
//        }
//
//        public Builder currency(CurrencyType currency) {
//            if (currency == null) throw new NullPointerException("currency type cannot be null.");
//            this.currency = currency;
//            return this;
//        }
//
//        public Builder investorId(long investorId) {
//            if (investorId == 0) throw new IllegalArgumentException("Investor Id must not be 0.");
//            this.investorId = investorId;
//            return this;
//        }
//
//        public Builder txIdentifier(String txIdentifier) {
//            if (txIdentifier == null)
//                throw new NullPointerException("transaction id cannot be null.");
//            if (txIdentifier.isEmpty())
//                throw new IllegalArgumentException("transaction id must not " + "be empty.");
//            this.txIdentifier = txIdentifier;
//            return this;
//        }
//    }

//private EligibleForRefund(Builder builder) {
//    this.refundReason = builder.refundReason;
//    this.amount = builder.amount;
//    this.currency = builder.currency;
//    this.investorId = builder.investorId;
//    this.txIdentifier = builder.txIdentifier;
//}
}
