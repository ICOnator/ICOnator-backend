package io.modum.tokenapp.backend.dto;

import javax.validation.constraints.NotNull;

public class AddressRequest {

    @NotNull
    private String address;

    @NotNull
    private String refundETH;

    @NotNull
    private String refundBTC;

    public String getAddress() {
        return address;
    }

    public AddressRequest setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getRefundETH() {
        return refundETH;
    }

    public AddressRequest setRefundETH(String refundETH) {
        this.refundETH = refundETH;
        return this;
    }

    public String getRefundBTC() {
        return refundBTC;
    }

    public AddressRequest setRefundBTC(String refundBTC) {
        this.refundBTC = refundBTC;
        return this;
    }

}
