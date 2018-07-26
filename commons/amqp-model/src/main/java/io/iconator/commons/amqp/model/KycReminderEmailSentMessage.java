package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KycReminderEmailSentMessage extends Message {

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
