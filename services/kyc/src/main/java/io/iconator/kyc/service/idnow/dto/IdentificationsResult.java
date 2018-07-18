package io.iconator.kyc.service.idnow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IdentificationsResult {

    @JsonProperty("identifications")
    private List<IdentificationResult> identifications;

    public IdentificationsResult() {
    }

    public List<IdentificationResult> getIdentifications() {
        return identifications;
    }
}
