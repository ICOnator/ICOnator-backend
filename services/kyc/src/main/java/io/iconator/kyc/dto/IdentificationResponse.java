package io.iconator.kyc.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.iconator.kyc.utils.IdentificationDeserializer;

import java.util.List;

@JsonDeserialize(using = IdentificationDeserializer.class)
public class IdentificationResponse {
    private List<Identification> identifications;

    public IdentificationResponse() {}

    public List<Identification> getIdentifications() {
        return identifications;
    }

    public void setIdentifications(List<Identification> identifications) {
        this.identifications = identifications;
    }
}
