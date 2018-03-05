package io.iconator.rates.client.etherscan.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TxReturnValueResult {

    @JsonProperty("blockNumber")
    public String blockNumber;

    @JsonProperty("timeStamp")
    public String timeStamp;

    @JsonProperty("hash")
    public String hash;

    @JsonProperty("nonce")
    public String nonce;

    @JsonProperty("blockHash")
    public String blockHash;

    @JsonProperty("transactionIndex")
    public String transactionIndex;

    @JsonProperty("from")
    public String from;

    @JsonProperty("to")
    public String to;

    @JsonProperty("value")
    public String value;

    @JsonProperty("gas")
    public String gas;

    @JsonProperty("gasPrice")
    public String gasPrice;

    @JsonProperty("isError")
    public String isError;

    @JsonProperty("input")
    public String input;

    @JsonProperty("contractAddress")
    public String contractAddress;

    @JsonProperty("cumulativeGasUsed")
    public String cumulativeGasUsed;

    @JsonProperty("gasUsed")
    public String gasUsed;

    @JsonProperty("confirmations")
    public String confirmations;

    public TxReturnValueResult() {
    }

    public TxReturnValueResult(String blockNumber, String timeStamp, String hash, String nonce, String blockHash, String transactionIndex, String from, String to, String value, String gas, String gasPrice, String isError, String input, String contractAddress, String cumulativeGasUsed, String gasUsed, String confirmations) {
        this.blockNumber = blockNumber;
        this.timeStamp = timeStamp;
        this.hash = hash;
        this.nonce = nonce;
        this.blockHash = blockHash;
        this.transactionIndex = transactionIndex;
        this.from = from;
        this.to = to;
        this.value = value;
        this.gas = gas;
        this.gasPrice = gasPrice;
        this.isError = isError;
        this.input = input;
        this.contractAddress = contractAddress;
        this.cumulativeGasUsed = cumulativeGasUsed;
        this.gasUsed = gasUsed;
        this.confirmations = confirmations;
    }

    public String getBlockNumber() {
        return blockNumber;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getHash() {
        return hash;
    }

    public String getNonce() {
        return nonce;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public String getTransactionIndex() {
        return transactionIndex;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getValue() {
        return value;
    }

    public String getGas() {
        return gas;
    }

    public String getGasPrice() {
        return gasPrice;
    }

    public String getIsError() {
        return isError;
    }

    public String getInput() {
        return input;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public String getCumulativeGasUsed() {
        return cumulativeGasUsed;
    }

    public String getGasUsed() {
        return gasUsed;
    }

    public String getConfirmations() {
        return confirmations;
    }
}
