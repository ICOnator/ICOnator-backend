package io.modum.tokenapp.backend.service.model;

import io.modum.tokenapp.backend.model.Investor;
import io.modum.tokenapp.backend.service.MailType;

import java.net.URI;
import java.util.Date;

public class Email {

    private MailType mailType;
    private Investor investor;
    private URI confirmationEmaiLink;
    private Date timestamp;

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

}
