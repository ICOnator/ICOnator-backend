package io.iconator.rates.client.blockr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TxReturnValueDataTransaction {

    @JsonProperty("tx")
    public String tx;

    @JsonProperty("time_utc")
    public String timeUtc;

    @JsonProperty("confirmations")
    public Integer confirmations;

    @JsonProperty("amount")
    public String amount;

    @JsonProperty("amount_multisig")
    public String amountMultisig;

    public TxReturnValueDataTransaction() {
    }

    public TxReturnValueDataTransaction(String tx, String timeUtc, Integer confirmations, String amount, String amountMultisig) {
        this.tx = tx;
        this.timeUtc = timeUtc;
        this.confirmations = confirmations;
        this.amount = amount;
        this.amountMultisig = amountMultisig;
    }

    public String getTx() {
        return tx;
    }

    public String getTimeUtc() {
        return timeUtc;
    }

    public Integer getConfirmations() {
        return confirmations;
    }

    public String getAmount() {
        return amount;
    }

    public String getAmountMultisig() {
        return amountMultisig;
    }
}
