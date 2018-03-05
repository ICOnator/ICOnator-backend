package io.iconator.rates.client.blockr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnValueData {

    @JsonProperty("nb")
    public Integer nb;

    @JsonProperty("hash")
    public String hash;

    @JsonProperty("version")
    public Integer version;

    @JsonProperty("confirmations")
    public Integer confirmations;

    @JsonProperty("time_utc")
    public String timeUtc;

    @JsonProperty("nb_txs")
    public Integer nbTxs;

    @JsonProperty("merkleroot")
    public String merkleroot;

    @JsonProperty("next_block_nb")
    public Boolean nextBlockNb;

    @JsonProperty("prev_block_nb")
    public Integer prevBlockNb;

    @JsonProperty("next_block_hash")
    public String nextBlockHash;

    @JsonProperty("prev_block_hash")
    public String prevBlockHash;

    @JsonProperty("fee")
    public String fee;

    @JsonProperty("vout_sum")
    public Double voutSum;

    @JsonProperty("size")
    public String size;

    @JsonProperty("difficulty")
    public Double difficulty;

    @JsonProperty("days_destroyed")
    public Double daysDestroyed;

    @JsonProperty("extras")
    public Object extras;

    public ReturnValueData() {
    }

    public ReturnValueData(Integer nb, String hash, Integer version, Integer confirmations, String timeUtc, Integer nbTxs, String merkleroot, Boolean nextBlockNb, Integer prevBlockNb, String nextBlockHash, String prevBlockHash, String fee, Double voutSum, String size, Double difficulty, Double daysDestroyed, Object extras) {
        this.nb = nb;
        this.hash = hash;
        this.version = version;
        this.confirmations = confirmations;
        this.timeUtc = timeUtc;
        this.nbTxs = nbTxs;
        this.merkleroot = merkleroot;
        this.nextBlockNb = nextBlockNb;
        this.prevBlockNb = prevBlockNb;
        this.nextBlockHash = nextBlockHash;
        this.prevBlockHash = prevBlockHash;
        this.fee = fee;
        this.voutSum = voutSum;
        this.size = size;
        this.difficulty = difficulty;
        this.daysDestroyed = daysDestroyed;
        this.extras = extras;
    }

    public Integer getNb() {
        return nb;
    }

    public String getHash() {
        return hash;
    }

    public Integer getVersion() {
        return version;
    }

    public Integer getConfirmations() {
        return confirmations;
    }

    public String getTimeUtc() {
        return timeUtc;
    }

    public Integer getNbTxs() {
        return nbTxs;
    }

    public String getMerkleroot() {
        return merkleroot;
    }

    public Boolean getNextBlockNb() {
        return nextBlockNb;
    }

    public Integer getPrevBlockNb() {
        return prevBlockNb;
    }

    public String getNextBlockHash() {
        return nextBlockHash;
    }

    public String getPrevBlockHash() {
        return prevBlockHash;
    }

    public String getFee() {
        return fee;
    }

    public Double getVoutSum() {
        return voutSum;
    }

    public String getSize() {
        return size;
    }

    public Double getDifficulty() {
        return difficulty;
    }

    public Double getDaysDestroyed() {
        return daysDestroyed;
    }

    public Object getExtras() {
        return extras;
    }
}
