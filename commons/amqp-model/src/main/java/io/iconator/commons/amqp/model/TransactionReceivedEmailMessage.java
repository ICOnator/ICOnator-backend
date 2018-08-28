package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.iconator.commons.amqp.model.dto.InvestorMessageDTO;
import io.iconator.commons.model.CurrencyType;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionReceivedEmailMessage extends IncludeTransactionInfoMessage {

    public TransactionReceivedEmailMessage() {
    }

    public TransactionReceivedEmailMessage(InvestorMessageDTO investor, BigDecimal amountFundsReceived, CurrencyType currencyType, String transactionUrl) {
        super(investor, amountFundsReceived, currencyType, transactionUrl);
    }

}
