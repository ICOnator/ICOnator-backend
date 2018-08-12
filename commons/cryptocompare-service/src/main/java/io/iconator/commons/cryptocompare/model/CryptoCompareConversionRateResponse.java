package io.iconator.commons.cryptocompare.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoCompareConversionRateResponse {

    @JsonIgnore
    private CryptoCompareCurrency cryptoCompareCurrency;

    @JsonIgnore
    private BigDecimal rateValue;

    public CryptoCompareConversionRateResponse() {
    }

    public CryptoCompareConversionRateResponse(CryptoCompareCurrency cryptoCompareCurrency, BigDecimal rateValue) {
        this.cryptoCompareCurrency = cryptoCompareCurrency;
        this.rateValue = rateValue;
    }

    public CryptoCompareCurrency getCryptoCompareCurrency() {
        return cryptoCompareCurrency;
    }

    public BigDecimal getRateValue() {
        return rateValue;
    }

    @JsonAnyGetter
    public Map<String, Object> anyGetter() {
        HashMap map = new HashMap<>();
        map.put(cryptoCompareCurrency.getName(), rateValue);
        return map;
    }

    @JsonAnySetter
    public void anySetter(String key, BigDecimal value) {
        this.cryptoCompareCurrency = CryptoCompareCurrency.valueOf(key);
        this.rateValue = value;
    }

}
