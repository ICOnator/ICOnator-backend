package io.modum.tokenapp.backend.service;


import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Blockr {
    //https://blockchain.info/q/getblockcount

    //http://tbtc.blockr.io/api/v1/block/info/last
    //http://btc.blockr.io/api/v1/block/info/last

    @Value("${modum.url.blockr:tbtc.blockr.io}")
    private String url; //api.etherscan.io or rinkeby.etherscan.io

    @Autowired
    private RestTemplate restTemplate;

    public long getCurrentBlockNr() {
        String s = "https://"+url+"/api/v1/block/info/last";

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "the mighty tokenapp-backend");

        ReturnValue retVal = restTemplate.exchange(s, HttpMethod.GET,new HttpEntity<>(null, headers), ReturnValue.class).getBody();
        return retVal.data.nb;
    }

    private static class ReturnValue {
        @JsonProperty("status")
        public String status;
        @JsonProperty("data")
        public Data data;
        @JsonProperty("code")
        public Integer code;
        @JsonProperty("message")
        public String message;
        public static class Data {
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
        }
    }
}