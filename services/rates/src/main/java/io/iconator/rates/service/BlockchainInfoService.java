package io.iconator.rates.service;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.rates.config.RatesAppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BlockchainInfoService {

    private final static Logger LOG = LoggerFactory.getLogger(BlockchainInfoService.class);


    public static String TEST_URL = "https://testnet.blockchain.info/latestblock";
    public static String MAIN_URL = "https://blockchain.info/latestblock";

    @Autowired
    private RatesAppConfig ratesAppConfig;

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public Long getLatestBitcoinHeight() throws IOException {

        final String url;
        if(ratesAppConfig.getBitcoinNet().equals("main")) {
            url = MAIN_URL;
        } else {
            url = TEST_URL;
        }

        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            BlockchainInfoReply reply = new ObjectMapper().readValue(jsonText, BlockchainInfoReply.class);
            LOG.debug("Bitcoin height: {}",reply.getHeight());
            return reply.getHeight();
        } finally {
            is.close();
        }
    }

    public static class BlockchainInfoReply {

        @JsonProperty("hash")
        private String hash;
        @JsonProperty("time")
        private Long time;
        @JsonProperty("block_index")
        private Long blockIndex;
        @JsonProperty("height")
        private Long height;
        @JsonProperty("txIndexes")
        private List<Long> txIndexes = null;
        @JsonIgnore
        private Map<String, Object> additionalProperties = new HashMap<>();

        @JsonProperty("hash")
        public String getHash() {
            return hash;
        }

        @JsonProperty("hash")
        public void setHash(String hash) {
            this.hash = hash;
        }

        @JsonProperty("time")
        public Long getTime() {
            return time;
        }

        @JsonProperty("time")
        public void setTime(Long time) {
            this.time = time;
        }

        @JsonProperty("block_index")
        public Long getBlockIndex() {
            return blockIndex;
        }

        @JsonProperty("block_index")
        public void setBlockIndex(Long blockIndex) {
            this.blockIndex = blockIndex;
        }

        @JsonProperty("height")
        public Long getHeight() {
            return height;
        }

        @JsonProperty("height")
        public void setHeight(Long height) {
            this.height = height;
        }

        @JsonProperty("txIndexes")
        public List<Long> getTxIndexes() {
            return txIndexes;
        }

        @JsonProperty("txIndexes")
        public void setTxIndexes(List<Long> txIndexes) {
            this.txIndexes = txIndexes;
        }

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

    }
}
