package io.iconator.rates.client.blockr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TxReturnValue {

    @JsonProperty("status")
    public String status;

    @JsonProperty("data")
    public TxReturnValueData data;

    @JsonProperty("code")
    public Integer code;

    @JsonProperty("message")
    public String message;

    public TxReturnValue() {
    }

    public TxReturnValue(String status, TxReturnValueData data, Integer code, String message) {
        this.status = status;
        this.data = data;
        this.code = code;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public TxReturnValueData getData() {
        return data;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
