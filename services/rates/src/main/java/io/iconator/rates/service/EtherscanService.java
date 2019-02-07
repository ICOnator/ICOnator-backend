package io.iconator.rates.service;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.rates.config.RatesAppConfigHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Service
public class EtherscanService {

    private final static Logger LOG = LoggerFactory.getLogger(EtherscanService.class);

    public final static String URL = "https://{ethnet}.etherscan.io/api?module=proxy&action=eth_blockNumber&apikey={ethkey}";

    @Autowired
    private RatesAppConfigHolder ratesAppConfig;

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public Long getLatestEthereumHeight() throws IOException {

        final String url;
        if (ratesAppConfig.getEthereumNet().equals("main")) {
            url = URL
                    .replace("{ethnet}", "api")
                    .replace("{ethkey}", ratesAppConfig.getEthereumKey());
        } else if (ratesAppConfig.getEthereumNet().isEmpty()) {
            url = URL
                    .replace("{ethnet}", "api-rinkeby");
        } else {
            url = URL
                    .replace("{ethnet}", "api-" + ratesAppConfig.getEthereumNet())
                    .replace("{ethkey}", ratesAppConfig.getEthereumKey());
        }


        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            EthrescanReply reply = new ObjectMapper().readValue(jsonText, EthrescanReply.class);
            try {
                Long height = Long.decode(reply.result);
                LOG.debug("Ethereum height: {}", height);
                return height;
            } catch (NumberFormatException nfe) {
                LOG.error("Fallback etherscan failed. Cannot convert to long", nfe);
                return null;
            }
        } finally {
            is.close();
        }
    }

    public static class EthrescanReply {

        @JsonProperty("jsonrpc")
        private String jsonrpc;
        @JsonProperty("id")
        private Integer id;
        @JsonProperty("result")
        private String result;
        @JsonIgnore
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        @JsonProperty("jsonrpc")
        public String getJsonrpc() {
            return jsonrpc;
        }

        @JsonProperty("jsonrpc")
        public void setJsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc;
        }

        @JsonProperty("id")
        public Integer getId() {
            return id;
        }

        @JsonProperty("id")
        public void setId(Integer id) {
            this.id = id;
        }

        @JsonProperty("result")
        public String getResult() {
            return result;
        }

        @JsonProperty("result")
        public void setResult(String result) {
            this.result = result;
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
