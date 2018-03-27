package io.iconator.commons.model.db;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Date;

@Entity
@Table(name="sale_tier")
public class SaleTier {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "id", updatable = false)
    private long id;

    @Column(name = "tier_no")
    private int tierNo;

    @Column(name = "description")
    private String description;

    @Temporal(TemporalType.DATE)
    @Column(name = "begin_date")
    private Date beginDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "discount")
    private double discount;

    @Column(name = "tokens_sold")
    private BigInteger tokensSold;

    @Column(name = "token_max")
    private BigInteger tokenMax;

    @Column(name = "is_active")
    private boolean isActive;

    protected SaleTier() {}

    public SaleTier(int tierNo, String description, Date beginDate, Date endDate,
                    double discount, BigInteger tokenMax, boolean isActive) {
        this.tierNo = tierNo;
        this.description = description;
        this.beginDate = beginDate;
        this.endDate = endDate;
        this.discount = discount;
        this.tokenMax = tokenMax;
        this.tokensSold = BigInteger.ZERO;
        this.isActive = isActive;
    }

    public int getTierNo() {
        return tierNo;
    }

    public String getDescription() {
        return description;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public double getDiscount() {
        return discount;
    }

    public BigInteger getTokensSold() {
        return tokensSold;
    }

    public BigInteger getTokenMax() {
        return tokenMax;
    }

    public boolean getActive() {
        return isActive;
    }
}
