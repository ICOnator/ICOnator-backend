package io.modum.tokenapp.backend.dto;

import io.modum.tokenapp.backend.utils.Constants;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class AddressRequest {

    @NotNull
    private String address;

    @Size(max = Constants.ETH_ADDRESS_CHAR_MAX_SIZE)
    private String refundETH;

    @Size(max = Constants.BTC_ADDRESS_CHAR_MAX_SIZE)
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
