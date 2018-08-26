package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.iconator.commons.amqp.model.dto.InvestorMessageDTO;
import io.iconator.commons.model.CurrencyType;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokensAllocatedEmailMessage extends IncludeTransactionInfoMessage {

    @JsonProperty("tokenAmount")
    private BigDecimal tokenAmount;

    public TokensAllocatedEmailMessage() {
    }

    public TokensAllocatedEmailMessage(InvestorMessageDTO investor, BigDecimal amountFundsReceived, CurrencyType currencyType, String transactionUrl, BigDecimal tokenAmount) {
        super(investor, amountFundsReceived, currencyType, transactionUrl);
        this.tokenAmount = tokenAmount;
    }

    public BigDecimal getTokenAmount() {
        return tokenAmount;
    }
}
