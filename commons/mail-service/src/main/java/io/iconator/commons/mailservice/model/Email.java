package io.iconator.commons.mailservice.model;

import io.iconator.commons.mailservice.MailType;
import io.iconator.commons.model.db.Investor;

import java.net.URI;
import java.util.Date;

public class Email {

    private MailType mailType;
    private Investor investor;
    private URI confirmationEmaiLink;
    private Date timestamp;
    private int reQueued;

    public Email(MailType mailType, Investor investor, URI confirmationEmaiLink) {
        this.mailType = mailType;
        this.investor = investor;
        this.confirmationEmaiLink = confirmationEmaiLink;
        this.timestamp = new Date();
    }

    public Email(MailType mailType, Investor investor) {
        this(mailType, investor, null);
    }

    public Email() {
    }

    public MailType getMailType() {
        return mailType;
    }

    public Investor getInvestor() {
        return investor;
    }

    public URI getConfirmationEmaiLink() {
        return confirmationEmaiLink;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public int getReQueued() {
        return reQueued;
    }

    public void setReQueued(int reQueued) {
        this.reQueued = reQueued;
    }

}
