package io.iconator.rates.client.etherscan.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnValuesResult {

    @JsonProperty("account")
    public String account;

    @JsonProperty("balance")
    public String balance;

    public ReturnValuesResult() {
    }

    public ReturnValuesResult(String account, String balance) {
        this.account = account;
        this.balance = balance;
    }

    public String getAccount() {
        return account;
    }

    public String getBalance() {
        return balance;
    }
}
