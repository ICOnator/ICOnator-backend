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
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoCompareResponseDTO {

    @JsonIgnore
    private List<CryptoCompareResponse> response = new ArrayList<>();

    public CryptoCompareResponseDTO() {
    }

    public CryptoCompareResponseDTO(List<CryptoCompareResponse> response) {
        this.response = response;
    }

    public List<CryptoCompareResponse> getResponse() {
        return response;
    }

    @JsonAnyGetter
    public Map<String, Object> anyGetter() {
        HashMap map = new HashMap<>(response.size());
        response.forEach((element) -> map.put(element.getCryptoCompareCurrency().getName(), element.getRates()));
        return map;
    }

    @JsonAnySetter
    public void anySetter(String key, CryptoCompareConversionRates value) {
        if (CryptoCompareCurrency.exists(key)) {
            this.response.add(new CryptoCompareResponse(CryptoCompareCurrency.valueOf(key), value));
        }
    }

    public Optional<BigDecimal> getRateValue(CryptoCompareCurrency fromCurrency, CryptoCompareCurrency toCurrency) {
        return this.getResponse().stream()
                .filter((response) -> response.getCryptoCompareCurrency() == fromCurrency)
                .map((response) -> response.getRates())
                .flatMap((toRates) ->
                        toRates.getConversionRateResponses().stream()
                                .filter((rateResponses) -> rateResponses.getCryptoCompareCurrency() == toCurrency)
                                .map((rateResponses) -> rateResponses.getRateValue())
                )
                .findFirst();
    }

}
