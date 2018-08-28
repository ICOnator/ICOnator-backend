package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.iconator.commons.model.CurrencyType;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchRatesRequestMessage extends Message {

    @JsonProperty("desiredRateTimestamp")
    private Date desiredRateTimestamp;

    @JsonProperty("from")
    private CurrencyType from;

    @JsonProperty("to")
    private List<CurrencyType> to;

    public FetchRatesRequestMessage() {
        super();
    }

    public FetchRatesRequestMessage(Date desiredRateTimestamp, CurrencyType from, List<CurrencyType> to) {
        super();
        this.desiredRateTimestamp = desiredRateTimestamp;
        this.from = from;
        this.to = to;
    }

    public Date getDesiredRateTimestamp() {
        return desiredRateTimestamp;
    }

    public CurrencyType getFrom() {
        return from;
    }

    public List<CurrencyType> getTo() {
        return to;
    }
}
