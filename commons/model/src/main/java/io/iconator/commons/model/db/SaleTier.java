package io.iconator.commons.model.db;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "sale_tier")
public class SaleTier {

    @Id
    @GeneratedValue(strategy = SEQUENCE)
    @Column(name = "id", updatable = false)
    private long id;

    @Version
    private Long version = 0L;

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

    @Column(name = "discount")
    private BigDecimal discount;

    @Column(name = "tokens_sold", precision = 34, scale = 0)
    private BigInteger tokensSold;

    @Column(name = "token_max", precision = 34, scale = 0)
    private BigInteger tokenMax;

    protected SaleTier() {
    }

    public SaleTier(int tierNo, String description, Date startDate, Date endDate,
                    BigDecimal discount, BigInteger tokenMax) {
        this.tierNo = tierNo;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.discount = discount;
        this.tokenMax = tokenMax;
        this.tokensSold = BigInteger.ZERO;
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
}
