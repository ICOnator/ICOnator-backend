package io.iconator.rates.client.etherscan.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnValues {

    @JsonProperty("status")
    public String status;

    @JsonProperty("message")
    public String message;

    @JsonProperty("result")
    public List<ReturnValuesResult> result = null;

    public ReturnValues() {
    }

    public ReturnValues(String status, String message, List<ReturnValuesResult> result) {
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

    public List<ReturnValuesResult> getResult() {
        return result;
    }
}
