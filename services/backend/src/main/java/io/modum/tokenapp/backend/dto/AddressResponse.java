package io.modum.tokenapp.backend.dto;

import io.modum.tokenapp.backend.utils.Constants;

import javax.validation.constraints.Size;

public class AddressResponse {

    @Size(max = Constants.ETH_ADDRESS_CHAR_MAX_SIZE)
    private String ether;

    @Size(max = Constants.BTC_ADDRESS_CHAR_MAX_SIZE)
    private String btc;

    public String getEther() {
        return ether;
    }

    public AddressResponse setEther(String ether) {
        this.ether = ether;
        return this;
    }

    public String getBtc() {
        return btc;
    }

    public AddressResponse setBtc(String btc) {
        this.btc = btc;
        return this;
    }

}
