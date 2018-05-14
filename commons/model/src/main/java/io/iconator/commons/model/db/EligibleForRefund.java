package io.iconator.commons.model.db;

import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.Unit;

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
        TOKEN_CONVERSION_FAILED,
        FAILED_CONVERSION_TO_USD
    }

    @Id
    @GeneratedValue(strategy = SEQUENCE)
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "refund_reason")
    @Enumerated(EnumType.STRING)
    private RefundReason refundReason;

    @Column(name = "amount")
    private BigInteger amount;

    @Column(name = "unit")
    @Enumerated(EnumType.STRING)
    private Unit unit;

    @Column(name = "currency")
    @Enumerated(EnumType.STRING)
    private CurrencyType currency;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "investor", nullable = true)
    private Investor investor;

    @Column(name = "tx_identifier", unique = true, nullable = false)
    private String txIdentifier;

    protected EligibleForRefund() {
    }

    public EligibleForRefund(RefundReason refundReason, BigInteger amount, Unit unit,
                             CurrencyType currency, Investor investor, String txIdentifier) {
        this.refundReason = refundReason;
        this.amount = amount;
        this.unit = unit;
        this.currency = currency;
        this.investor = investor;
        this.txIdentifier = txIdentifier;
    }

}
