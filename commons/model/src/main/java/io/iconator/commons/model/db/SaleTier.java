package io.iconator.commons.model.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@Entity
@Table(name = "sale_tier")
public class SaleTier {

    public enum StatusType {
        CLOSED,
        ACTIVE,
        INCOMING
    }

    @Version
    private Long version = 0L;

    @Id
    @Column(name = "tier_no")
    private long tierNo;

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

    @Column(name = "tomics_sold", precision = 34, scale = 0)
    private BigInteger tomicsSold;

    @Column(name = "tomics_max", precision = 34, scale = 0)
    private BigInteger tomicsMax;

    @Column(name = "has_dynamic_duration")
    private boolean hasDynamicDuration;

    @Column(name = "has_dynamic_max")
    private boolean hasDynamicMax;

    public SaleTier(long tierNo, String description, Date startDate, Date endDate, BigDecimal discount,
                    BigInteger tomicsSold, BigInteger tomicsMax, boolean hasDynamicDuration, boolean hasDynamicMax) {
        this.tierNo = tierNo;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.discount = discount;
        this.tomicsSold = tomicsSold;
        this.tomicsMax = tomicsMax;
        this.hasDynamicDuration = hasDynamicDuration;
        this.hasDynamicMax = hasDynamicMax;
    }

    protected SaleTier() {
    }

    public long getTierNo() {
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

    public BigInteger getTomicsSold() {
        return tomicsSold;
    }

    public void setTomicsSold(BigInteger tomicsSold) {
        this.tomicsSold = tomicsSold;
    }

    public BigInteger getTomicsMax() {
        return tomicsMax;
    }

    public void setTomicsMax(BigInteger tomicsMax) {
        this.tomicsMax = tomicsMax;
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
        return tomicsMax.compareTo(BigInteger.ZERO) > 0 && tomicsMax.compareTo(tomicsSold) == 0;
    }

    public boolean isAmountOverflowingTier(BigInteger tomics) {
        return tomicsSold.add(tomics).compareTo(tomicsMax) > 0;
    }

    public BigInteger getRemainingTomics() {
        return tomicsMax.subtract(tomicsSold);
    }

    public StatusType getStatusAtDate(Date date) {
        StatusType status;
        if (this.getEndDate().compareTo(date) <= 0) {
            status = StatusType.CLOSED;
        } else if (this.getStartDate().compareTo(date) <= 0 &&
                this.getEndDate().compareTo(date) > 0) {
            if (this.getTomicsSold().compareTo(this.getTomicsMax()) >= 0) {
                status = StatusType.CLOSED;
            } else {
                status = StatusType.ACTIVE;
            }
        } else {
            status = StatusType.INCOMING;
        }
        return status;
    }
}
