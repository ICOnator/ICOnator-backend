package io.iconator.kyc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KycStatusResponseDTO {

    @JsonProperty("investorId")
    private Long investorId;

    @JsonProperty("isSuccess")
    private Boolean isSuccess;

    public KycStatusResponseDTO() {}

    public KycStatusResponseDTO(Long investorId, Boolean isSuccess) {
        this.investorId = investorId;
        this.isSuccess = isSuccess;
    }

    public Long getInvestorId() {
        return investorId;
    }

    public Boolean getIsSuccess() {
        return isSuccess;
    }
}
