package io.iconator.rates.client.blockr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TxReturnValueData {

    @JsonProperty("address")
    public String address;

    @JsonProperty("limit_txs")
    public Integer limitTxs;

    @JsonProperty("nb_txs")
    public Integer nbTxs;

    @JsonProperty("nb_txs_displayed")
    public Integer nbTxsDisplayed;

    @JsonProperty("txs")
    public List<TxReturnValueDataTransaction> txs = null;

    public TxReturnValueData() {
    }

    public TxReturnValueData(String address, Integer limitTxs, Integer nbTxs, Integer nbTxsDisplayed, List<TxReturnValueDataTransaction> txs) {
        this.address = address;
        this.limitTxs = limitTxs;
        this.nbTxs = nbTxs;
        this.nbTxsDisplayed = nbTxsDisplayed;
        this.txs = txs;
    }

    public String getAddress() {
        return address;
    }

    public Integer getLimitTxs() {
        return limitTxs;
    }

    public Integer getNbTxs() {
        return nbTxs;
    }

    public Integer getNbTxsDisplayed() {
        return nbTxsDisplayed;
    }

    public List<TxReturnValueDataTransaction> getTxs() {
        return txs;
    }
}
