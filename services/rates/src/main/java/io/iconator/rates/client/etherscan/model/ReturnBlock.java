package io.iconator.rates.client.etherscan.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnBlock {

    @JsonProperty("id")
    public String id;

    @JsonProperty("jsonrpc")
    public String jsonrpc;

    @JsonProperty("result")
    public String result;

    public ReturnBlock() {
    }

    public ReturnBlock(String id, String jsonrpc, String result) {
        this.id = id;
        this.jsonrpc = jsonrpc;
        this.result = result;
    }

    public String getId() {
        return id;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public String getResult() {
        return result;
    }
}
