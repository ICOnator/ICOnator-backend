package io.iconator.kyc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompleteSingleKycResponseDTO {

    @JsonProperty("investorId")
    private Long investorId;

    @JsonProperty("isKycComplete")
    private Boolean isKycComplete;

    public CompleteSingleKycResponseDTO() {}

    public CompleteSingleKycResponseDTO(Long investorId, Boolean isKycComplete) {
        this.investorId = investorId;
        this.isKycComplete = isKycComplete;
    }

    public Long getInvestorId() {
        return investorId;
    }

    public Boolean getIsKycComplete() {
        return isKycComplete;
    }
}
