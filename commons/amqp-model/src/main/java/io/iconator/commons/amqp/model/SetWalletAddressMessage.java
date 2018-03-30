package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.iconator.commons.amqp.model.dto.InvestorMessageDTO;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SetWalletAddressMessage extends IncludeInvestorMessage {

    public SetWalletAddressMessage() {
        super();
    }

    public SetWalletAddressMessage(InvestorMessageDTO investor) {
        super(investor);
    }

}
