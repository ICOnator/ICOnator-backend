package io.iconator.kyc.service.idnow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IdNowIdentification {

    @JsonProperty("identificationprocess")
    private IdNowIdentificationProcess idNowIdentificationProcess;

    public IdNowIdentification() {
    }

    public IdNowIdentificationProcess getIdNowIdentificationProcess() {
        return idNowIdentificationProcess;
    }
}
