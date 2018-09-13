package io.iconator.kyc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RemindSingleKycResponseDTO {

    @JsonProperty("investorId")
    private Long investorId;

    @JsonProperty("isReminderSent")
    private Boolean isReminderSent;

    public RemindSingleKycResponseDTO() {}

    public RemindSingleKycResponseDTO(Long investorId, Boolean isReminderSent) {
        this.investorId = investorId;
        this.isReminderSent = isReminderSent;
    }

    public Long getInvestorId() {
        return investorId;
    }

    public Boolean getIsReminderSent() {
        return isReminderSent;
    }
}
