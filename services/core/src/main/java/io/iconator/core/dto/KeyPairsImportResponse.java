package io.iconator.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyPairsImportResponse {

    @JsonProperty("amountAdded")
    private Long amountAdded;

    @JsonProperty("amountNotAdded")
    private Long amountNotAdded;

    public KeyPairsImportResponse() {
    }

    public KeyPairsImportResponse(Long amountAdded, Long amountNotAdded) {
        this.amountAdded = amountAdded;
        this.amountNotAdded = amountNotAdded;
    }

    public Long getAmountAdded() {
        return amountAdded;
    }

    public Long getAmountNotAdded() {
        return amountNotAdded;
    }
}
