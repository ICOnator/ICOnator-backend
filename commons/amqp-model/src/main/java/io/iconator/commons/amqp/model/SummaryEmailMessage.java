package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.iconator.commons.amqp.model.dto.InvestorMessageDTO;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SummaryEmailMessage extends IncludeInvestorMessage {

    public SummaryEmailMessage() {
        super();
    }

    public SummaryEmailMessage(InvestorMessageDTO investor) {
        super(investor);
    }

}
