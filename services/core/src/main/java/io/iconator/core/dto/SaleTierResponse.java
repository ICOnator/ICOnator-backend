package io.iconator.core.dto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

public class SaleTierResponse {

    private int tierNo;

    private String name;

    @NotNull
    private Date startDate;

    @NotNull
    private Date endDate;

    @NotNull
    private BigDecimal discount;

    @NotNull
    private BigInteger amount;

    @NotNull
    private BigInteger maxAmount;

    public SaleTierResponse(int tierNo, String description, @NotNull Date startDate,
                            @NotNull Date endDate, @NotNull BigDecimal discount,
                            @NotNull BigInteger tokensSold, @NotNull BigInteger tokenMax) {
        this.tierNo = tierNo;
        this.name = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.discount = discount;
        this.amount = tokensSold;
        this.maxAmount = tokenMax;
    }

    private SaleTierResponse() {
    }

    public int getTierNo() {
        return tierNo;
    }

    public String getName() {
        return name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public BigInteger getMaxAmount() {
        return maxAmount;
    }
}
