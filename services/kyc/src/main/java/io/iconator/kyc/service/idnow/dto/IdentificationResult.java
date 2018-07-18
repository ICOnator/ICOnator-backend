package io.iconator.kyc.service.idnow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IdentificationResult {

    @JsonProperty("identificationprocess")
    private IdentificationProcess identificationProcess;

    public IdentificationResult() {
    }

    public IdentificationProcess getIdentificationProcess() {
        return identificationProcess;
    }
}
