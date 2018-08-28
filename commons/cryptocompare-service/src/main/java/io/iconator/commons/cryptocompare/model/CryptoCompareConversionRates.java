package io.iconator.commons.cryptocompare.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoCompareConversionRates {

    @JsonIgnore
    private List<CryptoCompareConversionRateResponse> conversionRateResponses = new ArrayList<>();

    public CryptoCompareConversionRates() {
    }

    public CryptoCompareConversionRates(List<CryptoCompareConversionRateResponse> conversionRateResponses) {
        this.conversionRateResponses = conversionRateResponses;
    }

    public List<CryptoCompareConversionRateResponse> getConversionRateResponses() {
        return conversionRateResponses;
    }

    @JsonAnyGetter
    public Map<String, Object> anyGetter() {
        HashMap map = new HashMap<>(conversionRateResponses.size());
        conversionRateResponses.forEach((element) -> map.put(element.getCryptoCompareCurrency().getName(), element.getRateValue()));
        return map;
    }

    @JsonAnySetter
    public void anySetter(String key, BigDecimal value) {
        if (CryptoCompareCurrency.exists(key)) {
            conversionRateResponses.add(new CryptoCompareConversionRateResponse(CryptoCompareCurrency.valueOf(key), value));
        }
    }

}
