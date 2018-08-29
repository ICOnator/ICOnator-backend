package io.iconator.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CountryFilterResponse {

    @JsonProperty("isAllowed")
    private Boolean isAllowed;

    public CountryFilterResponse() {
    }

    public CountryFilterResponse(Boolean isAllowed) {
        this.isAllowed = isAllowed;
    }

    public Boolean getAllowed() {
        return isAllowed;
    }
}
