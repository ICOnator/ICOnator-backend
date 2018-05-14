package io.iconator.core.dto;

public class CountryFilterResponse {

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
