package io.iconator.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.iconator.core.utils.Constants;

import javax.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressResponse {

    @Size(max = Constants.ETH_ADDRESS_CHAR_MAX_SIZE)
    @JsonProperty("ether")
    private String ether;

    @Size(max = Constants.BTC_ADDRESS_CHAR_MAX_SIZE)
    @JsonProperty("btc")
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
