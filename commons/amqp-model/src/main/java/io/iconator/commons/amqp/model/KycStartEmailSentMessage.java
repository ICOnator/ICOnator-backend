package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KycStartEmailSentMessage extends Message {

    @JsonProperty("emailAddress")
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
