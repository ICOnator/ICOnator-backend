package io.modum.tokenapp.rates.service;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.modum.tokenapp.rates.bean.Options;
import org.apache.commons.lang3.tuple.Triple;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class Blockr {

    private final static Logger LOG = LoggerFactory.getLogger(Blockr.class);
    //https://blockchain.info/q/getblockcount

    //http://tbtc.blockr.io/api/v1/block/info/last
    //http://btc.blockr.io/api/v1/block/info/last

    @Value("${modum.url.blockr:tbtc.blockr.io}")
    private String url; //api.etherscan.io or rinkeby.etherscan.io

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Options options;

    public long getCurrentBlockNr() throws IOException {
        String s = "https://" + url + "/api/v1/block/info/last";

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", options.getUserAgent());

        ResponseEntity<String> res = restTemplate.exchange(s, HttpMethod.GET, new HttpEntity<>(null, headers), String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        ReturnValue retVal = objectMapper.readValue(res.getBody(), ReturnValue.class);

        return retVal.data.nb;
    }

    public List<Triple<Date, Long, Long>> getTxBtc(String address) throws ParseException, IOException {
        String s = "https://" + url + "/api/v1/address/txs/" + address + "?amount_format=string";

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", options.getUserAgent());

        ResponseEntity<String> res = restTemplate.exchange(s, HttpMethod.GET, new HttpEntity<>(null, headers), String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        TxReturnValue retVal = objectMapper.readValue(res.getBody(), TxReturnValue.class);


        List<Triple<Date, Long, Long>> ret = new ArrayList<>();
        if (retVal.data.nbTxs != retVal.data.nbTxsDisplayed) {
            LOG.error("someone payed with over 200 tx, handle manually {}", address);
        }
        DateFormat m_ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        for (TxReturnValue.Data.Tx result : retVal.data.txs) {
            Date time = m_ISO8601Local.parse(result.timeUtc);
            BigDecimal amount = new BigDecimal(result.amount).multiply(new BigDecimal(100_000_000));
            ret.add(Triple.of(time, amount.longValue(), getBlockNr(result.tx)));
        }
        return ret;
    }

    public long getBlockNr(String tx) throws IOException {
        String s = "https://" + url + "/api/v1/tx/info/" + tx;

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", options.getUserAgent());

        ResponseEntity<String> res = restTemplate.exchange(s, HttpMethod.GET, new HttpEntity<>(null, headers), String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        TxInfoReturnValue retVal = objectMapper.readValue(res.getBody(), TxInfoReturnValue.class);

        return retVal.data.block;
    }

    public static class TxInfoReturnValue {

        @JsonProperty("status")
        public String status;
        @JsonProperty("data")
        public Data data;
        @JsonProperty("code")
        public Integer code;
        @JsonProperty("message")
        public String message;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Data {

            @JsonProperty("tx")
            public String tx;
            @JsonProperty("block")
            public Integer block;
            @JsonProperty("confirmations")
            public Integer confirmations;
            @JsonProperty("time_utc")
            public String timeUtc;


        }

    }

    public static class TxReturnValue {
        @JsonProperty("status")
        public String status;
        @JsonProperty("data")
        public Data data;
        @JsonProperty("code")
        public Integer code;
        @JsonProperty("message")
        public String message;

        public static class Data {
            @JsonProperty("address")
            public String address;
            @JsonProperty("limit_txs")
            public Integer limitTxs;
            @JsonProperty("nb_txs")
            public Integer nbTxs;
            @JsonProperty("nb_txs_displayed")
            public Integer nbTxsDisplayed;
            @JsonProperty("txs")
            public List<Tx> txs = null;

            public static class Tx {
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
            }
        }
    }


    public static class ReturnValue {
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




