package io.modum.tokenapp.backend.dto;

import javax.validation.constraints.NotNull;

public class AddressRequest {

    @NotNull
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
