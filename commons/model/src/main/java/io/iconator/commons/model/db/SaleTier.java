package io.iconator.commons.model.db;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * Uses integers for token amounts because it is assumed that tokens are stored in their smallest unit.
 */
@Entity
@Table(name = "sale_tier")
public class SaleTier {

    @Version
    private Long version = 0L;

    @Id
    @Column(name = "tier_no")
    private int tierNo;

    @Column(name = "description")
    private String description;

    @Temporal(TemporalType.DATE)
    @Column(name = "start_date")
    private Date startDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "discount", precision = 6, scale = 6)
    private BigDecimal discount;

    @Column(name = "tokens_sold", precision = 34, scale = 0)
    private BigInteger tokensSold;

    @Column(name = "token_max", precision = 34, scale = 0)
    private BigInteger tokenMax;

    public SaleTier(int tierNo, String description, Date startDate, Date endDate, BigDecimal discount,
                    BigInteger tokensSold, BigInteger tokenMax, boolean hasDynamicDuration, boolean hasDynamicMax) {
        this.tierNo = tierNo;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.discount = discount;
        this.tokensSold = tokensSold;
        this.tokenMax = tokenMax;
        this.hasDynamicDuration = hasDynamicDuration;
        this.hasDynamicMax = hasDynamicMax;
    }

    @Column(name = "has_dynamic_duration")
    private boolean hasDynamicDuration;

    @Column(name = "has_dynamic_max")
    private boolean hasDynamicMax;

    protected SaleTier() {
    }

    public int getTierNo() {
        return tierNo;
    }

    public void setTierNo(int tierNo) {
        this.tierNo = tierNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigInteger getTokensSold() {
        return tokensSold;
    }

    public void setTokensSold(BigInteger tokensSold) {
        this.tokensSold = tokensSold;
    }

    public BigInteger getTokenMax() {
        return tokenMax;
    }

    public void setTokenMax(BigInteger tokenMax) {
        this.tokenMax = tokenMax;
    }

    public boolean hasDynamicDuration() {
        return hasDynamicDuration;
    }

    public void setHasDynamicDuration(boolean hasDynamicDuration) {
        this.hasDynamicDuration = hasDynamicDuration;
    }

    public boolean hasDynamicMax() {
        return hasDynamicMax;
    }

    public void setHasDynamicMax(boolean hasDynamicMax) {
        this.hasDynamicMax = hasDynamicMax;
    }

    public boolean isFull() {
        return tokenMax == tokensSold;
    }

    public boolean isAmountOverflowingTier(BigInteger tokens) {
        return tokensSold.add(tokens).compareTo(tokenMax) > 0;
    }

    public BigInteger getRemainingTokens() {
        return tokenMax.subtract(tokensSold);
    }
}
