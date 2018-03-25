package io.iconator.rates.client.etherscan;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.google.common.collect.Lists;
import io.iconator.rates.client.etherscan.model.ReturnBlock;
import io.iconator.rates.client.etherscan.model.ReturnSingleValue;
import io.iconator.rates.client.etherscan.model.ReturnValues;
import io.iconator.rates.client.etherscan.model.ReturnValuesResult;
import io.iconator.rates.config.RatesAppConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class EtherScanClient {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RatesAppConfig ratesAppConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Retryer retryer;

    public BigInteger getBalance(String address) throws ExecutionException, RetryException {
        return (BigInteger) retryer.call(() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", ratesAppConfig.getUserAgent());

            UriComponents uriComponents = UriComponentsBuilder.fromUriString(ratesAppConfig.getEtherScanUrl())
                    .queryParam("module", "account")
                    .queryParam("action", "balance")
                    .queryParam("address", address)
                    .queryParam("tag", "latest")
                    .queryParam("apikey", ratesAppConfig.getEtherScanApiToken())
                    .build();

            ResponseEntity<String> res = restTemplate.exchange(uriComponents.toUri(),
                    HttpMethod.GET, new HttpEntity<>(null, headers), String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            ReturnSingleValue retVal = objectMapper.readValue(res.getBody(), ReturnSingleValue.class);
            return new BigInteger(retVal.result);
        });
    }

    /**
     * Can process up to 20 contracts
     */
    public BigInteger get20Balances(String... contract) throws ExecutionException, RetryException {
        return get20Balances(Arrays.asList(contract));
    }

    /**
     * Can process up to 20 contracts
     */
    public BigInteger get20Balances(List<String> contract) throws ExecutionException, RetryException {
        return (BigInteger) retryer.call(() -> {
            String addresses = String.join(",", contract);
            UriComponents uriComponents = UriComponentsBuilder.fromUriString(ratesAppConfig.getEtherScanUrl())
                    .queryParam("module", "account")
                    .queryParam("action", "balancemulti")
                    .queryParam("address", addresses)
                    .queryParam("tag", "latest")
                    .queryParam("apikey", ratesAppConfig.getEtherScanApiToken())
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", ratesAppConfig.getUserAgent());

            ResponseEntity<String> res = restTemplate.exchange(uriComponents.toUri(),
                    HttpMethod.GET, new HttpEntity<>(null, headers), String.class);
            ReturnValues retVal = objectMapper.readValue(res.getBody(), ReturnValues.class);

            BigInteger result = BigInteger.ZERO;
            for (ReturnValuesResult res1 : retVal.result) {
                result = result.add(new BigInteger(res1.balance));
            }
            return result;
        });
    }

    public long getCurrentBlockNr() throws ExecutionException, RetryException {
        return (long) retryer.call(() -> {
            UriComponents uriComponents = UriComponentsBuilder.fromUriString(ratesAppConfig.getEtherScanUrl())
                    .queryParam("module", "proxy")
                    .queryParam("action", "eth_blockNumber")
                    .queryParam("apikey", ratesAppConfig.getEtherScanApiToken())
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", ratesAppConfig.getUserAgent());

            ResponseEntity<String> res = restTemplate.exchange(uriComponents.toUri(),
                    HttpMethod.GET, new HttpEntity<>(null, headers), String.class);
            ReturnBlock retVal = objectMapper.readValue(res.getBody(), ReturnBlock.class);

            return Long.parseLong(retVal.result.substring(2), 16);
        });
    }

    /**
     * This may take a while, make sure you obey the limits of the api provider
     */
    public BigInteger getBalances(List<String> contract) throws ExecutionException, RetryException {
        BigInteger result = BigInteger.ZERO;
        List<List<String>> part = Lists.partition(contract, 20);
        for (List<String> p : part) {
            result = result.add(get20Balances(p));
        }
        return result;
        //TODO:caching!
    }

}
