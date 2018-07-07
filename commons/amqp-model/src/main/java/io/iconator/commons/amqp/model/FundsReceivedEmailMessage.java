package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.iconator.commons.amqp.model.dto.InvestorMessageDTO;
import io.iconator.commons.model.CurrencyType;

import java.math.BigDecimal;
import java.math.BigInteger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FundsReceivedEmailMessage extends IncludeInvestorMessage {

    private BigDecimal amountFundsReceived;

    private CurrencyType currencyType;

    private String linkToTransaction;

    private BigDecimal tokenAmount;

    public FundsReceivedEmailMessage() {
        super();
    }

    public FundsReceivedEmailMessage(BigDecimal amountFundsReceived, CurrencyType currencyType, String linkToTransaction, BigDecimal tokenAmount) {
        super();
        this.amountFundsReceived = amountFundsReceived;
        this.currencyType = currencyType;
        this.linkToTransaction = linkToTransaction;
        this.tokenAmount = tokenAmount;
    }

    public FundsReceivedEmailMessage(InvestorMessageDTO investor, BigDecimal amountFundsReceived, CurrencyType currencyType, String linkToTransaction, BigDecimal tokenAmount) {
        super(investor);
        this.amountFundsReceived = amountFundsReceived;
        this.currencyType = currencyType;
        this.linkToTransaction = linkToTransaction;
        this.tokenAmount = tokenAmount;
    }

    public BigDecimal getAmountFundsReceived() {
        return amountFundsReceived;
    }

    public CurrencyType getCurrencyType() {
        return currencyType;
    }

    public String getLinkToTransaction() {
        return linkToTransaction;
    }

    public BigDecimal getTokenAmount() {
        return tokenAmount;
    }
}
