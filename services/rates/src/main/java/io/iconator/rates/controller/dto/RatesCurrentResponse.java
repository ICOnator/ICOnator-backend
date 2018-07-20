package io.iconator.rates.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RatesCurrentResponse {

    @JsonProperty("btcPrice")
    private BigDecimal btcPrice;

    @JsonProperty("ethPrice")
    private BigDecimal ethPrice;

    public RatesCurrentResponse() {
    }

    public RatesCurrentResponse(BigDecimal btcPrice, BigDecimal ethPrice) {
        this.btcPrice = btcPrice;
        this.ethPrice = ethPrice;
    }

    public BigDecimal getBtcPrice() {
        return btcPrice;
    }

    public BigDecimal getEthPrice() {
        return ethPrice;
    }
}
