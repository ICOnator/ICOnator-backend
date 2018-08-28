package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public abstract class Message {

    @JsonProperty("messageTimestamp")
    private Date messageTimestamp;

    public Message() {
        this.messageTimestamp = new Date();
    }

    public Date getMessageTimestamp() {
        return messageTimestamp;
    }

    public void setMessageTimestamp(Date messageTimestamp) {
        this.messageTimestamp = messageTimestamp;
    }

}
