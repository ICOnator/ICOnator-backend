package io.iconator.commons.amqp.model;

import java.util.Date;

public abstract class Message {

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
