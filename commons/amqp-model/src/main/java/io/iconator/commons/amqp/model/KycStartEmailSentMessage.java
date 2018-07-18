package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.iconator.commons.amqp.model.dto.InvestorMessageDTO;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KycStartEmailSentMessage extends Message {

    private String emailAddress;

    public KycStartEmailSentMessage() {
        super();
    }

    public KycStartEmailSentMessage(String emailAddress) {
        super();
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

}
