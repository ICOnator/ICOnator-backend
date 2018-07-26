package io.iconator.kyc.service.idnow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IdNowIdentificationResponse {

    @JsonProperty("identifications")
    private List<IdNowIdentification> identifications;

    public IdNowIdentificationResponse() {
    }

    public List<IdNowIdentification> getIdentifications() {
        return identifications;
    }
}
