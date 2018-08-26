package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.iconator.commons.amqp.model.dto.InvestorMessageDTO;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfirmationEmailMessage extends IncludeInvestorMessage {

    @JsonProperty("emailLinkUri")
    private String emailLinkUri;

    public ConfirmationEmailMessage() {
        super();
    }

    public ConfirmationEmailMessage(String emailLinkUri) {
        super();
        this.emailLinkUri = emailLinkUri;
    }

    public ConfirmationEmailMessage(InvestorMessageDTO investor, String emailLinkUri) {
        super(investor);
        this.emailLinkUri = emailLinkUri;
    }

    public String getEmailLinkUri() {
        return emailLinkUri;
    }

}
