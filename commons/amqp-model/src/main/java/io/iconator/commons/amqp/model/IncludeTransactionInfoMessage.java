package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.iconator.commons.amqp.model.dto.InvestorMessageDTO;
import io.iconator.commons.model.CurrencyType;

import java.math.BigDecimal;

public abstract class IncludeTransactionInfoMessage extends IncludeInvestorMessage {

    @JsonProperty("amountFundsReceived")
    private BigDecimal amountFundsReceived;

    @JsonProperty("currencyType")
    private CurrencyType currencyType;

    @JsonProperty("transactionUrl")
    private String transactionUrl;

    public IncludeTransactionInfoMessage() {
        super();
    }

    public IncludeTransactionInfoMessage(InvestorMessageDTO investor, BigDecimal amountFundsReceived, CurrencyType currencyType, String transactionUrl) {
        super(investor);
        this.amountFundsReceived = amountFundsReceived;
        this.currencyType = currencyType;
        this.transactionUrl = transactionUrl;
    }

    public BigDecimal getAmountFundsReceived() {
        return amountFundsReceived;
    }

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    public String getTransactionUrl() {
        return transactionUrl;
    }
}
