package io.iconator.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SaleTierRequest {

    @JsonProperty("tierNo")
    private Long tierNo;

    @NotNull
    @JsonProperty("description")
    private String description;

    @NotNull
    @JsonProperty("startDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date startDate;

    @NotNull
    @JsonProperty("endDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date endDate;

    @NotNull
    @JsonProperty("discount")
    private BigDecimal discount;

    @NotNull
    @JsonProperty("tomicsSold")
    private BigInteger tomicsSold;

    @NotNull
    @JsonProperty("tomicsMax")
    private BigInteger tomicsMax;

    @NotNull
    @JsonProperty("hasDynamicDuration")
    private Boolean hasDynamicDuration;

    @NotNull
    @JsonProperty("hasDynamicMax")
    private Boolean hasDynamicMax;

    public SaleTierRequest() {
    }

    public SaleTierRequest(long tierNo, @NotNull String description, @NotNull Date startDate, @NotNull Date endDate, @NotNull BigDecimal discount, @NotNull BigInteger tomicsSold, @NotNull BigInteger tomicsMax, @NotNull Boolean hasDynamicDuration, @NotNull Boolean hasDynamicMax) {
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

    public long getTierNo() {
        return tierNo;
    }

    public String getDescription() {
        return description;
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

    public BigInteger getTomicsSold() {
        return tomicsSold;
    }

    public BigInteger getTomicsMax() {
        return tomicsMax;
    }

    public Boolean getHasDynamicDuration() {
        return hasDynamicDuration;
    }

    public Boolean getHasDynamicMax() {
        return hasDynamicMax;
    }
}
