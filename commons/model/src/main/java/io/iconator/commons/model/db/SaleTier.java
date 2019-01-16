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

/**
 * An ICO can be divided into multiple sale tiers, each with its own properties. This entity
 * represents that concept.
 */
@Entity
@Table(name = "sale_tier")
public class SaleTier {

    /**
     * A sale tier can be in one of these three status.
     */
    public enum StatusType {
        /* The sale tier is either currently not active (current date is not in his actie date
        range) or it is filled up. (no more tokens can be taken from the tier) */
        CLOSED,
        /* The sale tier is active (current date is in his actie date range) and it is not filled
        up.*/
        ACTIVE,
        /* The sale tier's active date range lies in the future. */
        INCOMING
    }

    @Version
    private Long version = 0L;

    @Id
    @Column(name = "tier_no")
    private long tierNo;

    /**
     * A description of the tier. This is can be useful when sending tier information to front ends.
     */
    @Column(name = "description")
    private String description;

    /**
     * The date and time at which the tier becomes active.
     * Note: Two tiers must not be active at the same time. I.e. their date ranges must not overlap.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "start_date")
    private Date startDate;

    /**
     * The date and time at which the tier is closed after being active.
     * Note: Two tiers must not be active at the same time. I.e. their date ranges must not overlap.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "end_date")
    private Date endDate;

    /**
     * The discount that is applied to token prices for tokens sold while this tier is active.
     * Applicable value range: 0 to 1 exclusive. 1 can't be used because tokens cannot be given away
     * for free with the ICOnator.
     */
    @Column(name = "discount", precision = 6, scale = 6)
    private BigDecimal discount;

    /**
     * The amount of tokens sold from this tier. Stored in the atomic unit of the token (Tomics).
     */
    @Column(name = "tomics_sold", precision = 34, scale = 0)
    private BigInteger tomicsSold;

    /**
     * The token limit of this sale tier. Stored in the atomic unit of the token (Tomics).
     * Improtant: If {@link SaleTier#hasDynamicMax} is set to true then make sure that the token
     * limit is * either initialized with 0 or null.
     */
    @Column(name = "tomics_max", precision = 34, scale = 0)
    private BigInteger tomicsMax;

    /**
     * If true, then this sale tier's end date will be adapted as soon as its token limit is
     * reached. The end date will be set to the time at which the limit is reached. All start and
     * end dates of tiers following this one will also be shifted.
     */
    @Column(name = "has_dynamic_duration")
    private boolean hasDynamicDuration;

    /**
     * If true, then the token limit of this tier will be dynamically set as soon as it becomes
     * active. The token limit depends on how many tokens have been sold at the time this tier
     * becomes active and what the total token limit of the ICO is.
     * Improtant: If this attribute is set to true then make sure that {@link SaleTier#tomicsMax} is
     * either initialized with 0 or null.
     */
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

    /**
     * Checks if the given token amount (in atomic units) overflows the tier's token limit.
     * The tokens sold so far are taken into account.
     * @param tomics Amount of tokens in atomic unit.
     * @return true if the given token amount overflows the tier's token limit given the current
     * amount of sold tokens. False, otherwise.
     */
    public boolean isAmountOverflowingTier(BigInteger tomics) {
        return tomicsSold.add(tomics).compareTo(tomicsMax) > 0;
    }

    /**
     * @return the amount of tokens that can still be sold from this tier.
     */
    public BigInteger getRemainingTomics() {
        return tomicsMax.subtract(tomicsSold);
    }

    /**
     * Calculates the status of the sale tier according to his current properties.
     * See {@link StatusType} for how which status is determined.
     *
     * @param date The date at which to determine the tiers status. Usually one will want to use the
     *             current date.
     * @return the status of the sale tier at the given date.
     */
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
