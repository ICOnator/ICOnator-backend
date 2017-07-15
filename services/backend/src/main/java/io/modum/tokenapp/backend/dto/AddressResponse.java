package io.modum.tokenapp.backend.dto;

public class AddressResponse {

    private String ether;
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
