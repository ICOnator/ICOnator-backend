package io.modum.tokenapp.rates.service;


import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Triple;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class Etherscan {
    @Value("${modum.token.etherscan:YourApiKeyToken}")
    private String apiKey;

    @Value("${modum.url.etherscan:rinkeby.etherscan.io}")
    private String url; //api.etherscan.io or rinkeby.etherscan.io

    @Autowired
    private RestTemplate restTemplate;

    public BigInteger getBalance(String address) throws IOException {
        String s = "https://"+url+"/api" +
                "?module=account" +
                "&action=balance" +
                "&address=" + address +
                "&tag=latest" +
                "&apikey="+apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "the mighty tokenapp-minting");

        ResponseEntity<String> res = restTemplate.exchange(s, HttpMethod.GET, new HttpEntity<>(null, headers), String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        ReturnSingleValue retVal = objectMapper.readValue(res.getBody(), ReturnSingleValue.class);
        return new BigInteger(retVal.result);
    }

    public List<Triple<Date,Long,Long>> getTxEth(String address) throws IOException {
        String s = "https://"+url+"/api" +
                "?module=account" +
                "&action=txlist" +
                "&address=" + address +
                "&tag=latest" +
                "&apikey="+apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "the mighty tokenapp-minting");

        ResponseEntity<String> res = restTemplate.exchange(s, HttpMethod.GET, new HttpEntity<>(null, headers), String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        TxReturnValue retVal = objectMapper.readValue(res.getBody(), TxReturnValue.class);

        List<Triple<Date,Long,Long>> ret = new ArrayList<>();
        for(TxReturnValue.Result result:retVal.result) {
            long time = Long.parseLong(result.timeStamp) * 1000;
            long value = Long.parseLong(result.value);
            long blocknr = Long.parseLong(result.blockNumber);
            ret.add(Triple.of(new Date(time), value, blocknr));
        }
        return ret;
    }

    /**
     * Can process up to 20 contracts
     */
    public BigInteger get20Balances(String... contract) throws IOException {
        return get20Balances(Arrays.asList(contract));
    }

    /**
     * Can process up to 20 contracts
     */
    public BigInteger get20Balances(List<String> contract) throws IOException {

        String addresses = String.join(",", contract);
        String s = "https://"+url+"/api" +
                "?module=account" +
                "&action=balancemulti" +
                "&address=" + addresses +
                "&tag=latest" +
                "&apikey="+apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "the mighty tokenapp-minting");

        ResponseEntity<String> res = restTemplate.exchange(s, HttpMethod.GET, new HttpEntity<>(null, headers), String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        ReturnValues retVal = objectMapper.readValue(res.getBody(), ReturnValues.class);

        BigInteger result = BigInteger.ZERO;
        for(ReturnValues.Result res1: retVal.result) {
            result = result.add(new BigInteger(res1.balance));
        }
        return result;
    }

    public long getCurrentBlockNr() throws IOException {
        String s = "https://"+url+"/api" +
                "?module=proxy" +
                "&action=eth_blockNumber" +
                "&apikey="+apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "the mighty tokenapp-minting");

        ResponseEntity<String> res = restTemplate.exchange(s, HttpMethod.GET, new HttpEntity<>(null, headers), String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        ReturnBlock retVal = objectMapper.readValue(res.getBody(), ReturnBlock.class);

        return Long.parseLong(retVal.result.substring(2), 16);
    }

    /**
     * This may take a while, make sure you obey the limits of the api provider
     */
    public BigInteger getBalances(List<String> contract) throws IOException {
        BigInteger result = BigInteger.ZERO;
        List<List<String>> part = Lists.partition(contract, 20);
        for(List<String> p:part) {
            result = result.add(get20Balances(p));
        }
        return result;
        //TODO:caching!
    }

    public static class TxReturnValue {

        @JsonProperty("status")
        public String status;
        @JsonProperty("message")
        public String message;
        @JsonProperty("result")
        public List<Result> result = null;

        public static class Result {

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

        }

    }

    private static class ReturnSingleValue {
        @JsonProperty("status")
        public String status;
        @JsonProperty("message")
        public String message;
        @JsonProperty("result")
        public String result;
    }

    private static class ReturnValues {
        @JsonProperty("status")
        public String status;
        @JsonProperty("message")
        public String message;
        @JsonProperty("result")
        public List<Result> result = null;
        public static class Result {
            @JsonProperty("account")
            public String account;
            @JsonProperty("balance")
            public String balance;
        }
    }

    private static class ReturnBlock {
        @JsonProperty("jsonrpc")
        public String jsonrpc;
        @JsonProperty("result")
        public String result;
        @JsonProperty("id")
        public String id;
    }
}
