package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.iconator.commons.amqp.model.dto.InvestorMessageDTO;

public abstract class IncludeInvestorMessage extends Message {

    @JsonProperty("investor")
    private InvestorMessageDTO investor;

    public IncludeInvestorMessage() {
        super();
    }

    public IncludeInvestorMessage(InvestorMessageDTO investor) {
        super();
        this.investor = investor;
    }

    public InvestorMessageDTO getInvestor() {
        return investor;
    }

    public void setInvestor(InvestorMessageDTO investor) {
        this.investor = investor;
    }

}
