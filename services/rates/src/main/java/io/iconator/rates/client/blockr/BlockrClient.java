package io.iconator.rates.client.blockr;


import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import io.iconator.rates.client.blockr.model.ReturnValue;
import io.iconator.rates.client.blockr.model.TxInfoReturnValue;
import io.iconator.rates.client.blockr.model.TxReturnValue;
import io.iconator.rates.client.blockr.model.TxReturnValueDataTransaction;
import io.iconator.rates.config.RatesAppConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class BlockrClient {

    private final static Logger LOG = LoggerFactory.getLogger(BlockrClient.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RatesAppConfig ratesAppConfig;

    @Autowired
    private Retryer retryer;

    public long getCurrentBlockNr() throws ExecutionException, RetryException {
        return (long) retryer.call(() -> {
            UriComponents uriComponents = UriComponentsBuilder.fromUriString(ratesAppConfig.getBlockrUrl())
                    .path("v1")
                    .path("block")
                    .path("info")
                    .path("last")
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", ratesAppConfig.getUserAgent());

            ResponseEntity<String> res = restTemplate.exchange(uriComponents.toUri(),
                    HttpMethod.GET, new HttpEntity<>(null, headers), String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            ReturnValue retVal = objectMapper.readValue(res.getBody(), ReturnValue.class);

            return retVal.data.nb;
        });
    }

    public List<Triple<Date, Long, Long>> getTxBtc(String address) throws ExecutionException, RetryException {
        return (List) retryer.call(() -> {
            UriComponents uriComponents = UriComponentsBuilder.fromUriString(ratesAppConfig.getBlockrUrl())
                    .path("v1")
                    .path("address")
                    .path("txs")
                    .path(address)
                    .queryParam("amount_format", "string")
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", ratesAppConfig.getUserAgent());

            ResponseEntity<String> res = restTemplate.exchange(uriComponents.toUri(),
                    HttpMethod.GET, new HttpEntity<>(null, headers), String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            TxReturnValue retVal = objectMapper.readValue(res.getBody(), TxReturnValue.class);

            List<Triple<Date, Long, Long>> ret = new ArrayList<>();
            if (retVal.data.nbTxs != retVal.data.nbTxsDisplayed) {
                LOG.error("someone payed with over 200 tx, handle manually {}", address);
            }
            DateFormat m_ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            for (TxReturnValueDataTransaction result : retVal.data.txs) {
                Date time = m_ISO8601Local.parse(result.timeUtc);
                BigDecimal amount = new BigDecimal(result.amount).multiply(new BigDecimal(100_000_000));
                ret.add(Triple.of(time, amount.longValue(), getBlockNr(result.tx)));
            }
            return ret;
        });
    }

    public long getBlockNr(String tx) throws ExecutionException, RetryException {
        return (long) retryer.call(() -> {
            UriComponents uriComponents = UriComponentsBuilder.fromUriString(ratesAppConfig.getBlockrUrl())
                    .path("v1")
                    .path("tx")
                    .path("info")
                    .path(tx)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", ratesAppConfig.getUserAgent());

            ResponseEntity<String> res = restTemplate.exchange(uriComponents.toUri(),
                    HttpMethod.GET, new HttpEntity<>(null, headers), String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            TxInfoReturnValue retVal = objectMapper.readValue(res.getBody(), TxInfoReturnValue.class);

            return retVal.data.block;
        });
    }

}




