package io.iconator.core.dto;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

public class SaleTierResponse {

    public enum StatusType {
        CLOSED,
        ACTIVE,
        INCOMING
    }

    private long tierNo;

    @NotNull
    private String name;

    @NotNull
    private StatusType type;

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

    public SaleTierResponse(long tierNo, @NotNull String name, @NotNull StatusType statusType,
                            @NotNull Date startDate, @NotNull Date endDate,
                            @NotNull BigDecimal discount, @NotNull BigInteger amount,
                            @NotNull BigInteger maxAmount) {

        this.tierNo = tierNo;
        this.name = name;
        this.type = statusType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.discount = discount;
        this.amount = amount;
        this.maxAmount = maxAmount;
    }

    private SaleTierResponse() {
    }

    public long getTierNo() {
        return tierNo;
    }

    public String getName() {
        return name;
    }

    public StatusType getType() {
        return type;
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
