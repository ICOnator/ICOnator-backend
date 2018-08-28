package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KycReminderEmailSentMessage extends Message {

    @JsonProperty("emailAddress")
    private String emailAddress;

    public KycReminderEmailSentMessage() {
        super();
    }

    public KycReminderEmailSentMessage(String emailAddress) {
        super();
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

}
