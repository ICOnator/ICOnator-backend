package io.modum.tokenapp.backend.dto;

public class AddressResponse {

    private String ether;
    private String btc;

    public String getEther() {
        return ether;
    }

    public void setEther(String ether) {
        this.ether = ether;
    }

    public String getBtc() {
        return btc;
    }

    public void setBtc(String btc) {
        this.btc = btc;
    }

}
