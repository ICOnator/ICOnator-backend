package io.iconator.commons.cryptocompare.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoCompareResponse {

    @JsonIgnore
    private CryptoCompareCurrency cryptoCompareCurrency;

    @JsonIgnore
    private CryptoCompareConversionRates rates;

    public CryptoCompareResponse() {
    }

    public CryptoCompareResponse(CryptoCompareCurrency cryptoCompareCurrency, CryptoCompareConversionRates rates) {
        this.cryptoCompareCurrency = cryptoCompareCurrency;
        this.rates = rates;
    }

    public CryptoCompareCurrency getCryptoCompareCurrency() {
        return cryptoCompareCurrency;
    }

    public CryptoCompareConversionRates getRates() {
        return rates;
    }

}
