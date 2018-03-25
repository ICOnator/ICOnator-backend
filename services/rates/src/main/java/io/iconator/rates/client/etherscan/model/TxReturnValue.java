package io.iconator.rates.client.etherscan.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TxReturnValue {

    @JsonProperty("status")
    public String status;

    @JsonProperty("message")
    public String message;

    @JsonProperty("result")
    public List<TxReturnValueResult> result = null;

    public TxReturnValue() {
    }

    public TxReturnValue(String status, String message, List<TxReturnValueResult> result) {
        this.status = status;
        this.message = message;
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<TxReturnValueResult> getResult() {
        return result;
    }
}
