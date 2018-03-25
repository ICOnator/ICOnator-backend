package io.iconator.rates.client.blockr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class TxInfoReturnValueData {

    @JsonProperty("tx")
    public String tx;

    @JsonProperty("block")
    public Integer block;

    @JsonProperty("confirmations")
    public Integer confirmations;

    @JsonProperty("time_utc")
    public String timeUtc;

    public TxInfoReturnValueData() {
    }

    public TxInfoReturnValueData(String tx, Integer block, Integer confirmations, String timeUtc) {
        this.tx = tx;
        this.block = block;
        this.confirmations = confirmations;
        this.timeUtc = timeUtc;
    }

    public String getTx() {
        return tx;
    }

    public Integer getBlock() {
        return block;
    }

    public Integer getConfirmations() {
        return confirmations;
    }

    public String getTimeUtc() {
        return timeUtc;
    }
}
