package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.iconator.commons.amqp.model.dto.InvestorMessageDTO;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KycReminderEmailMessage extends IncludeInvestorMessage {

    @JsonProperty("kycLinkUri")
    private String kycLinkUri;

    public KycReminderEmailMessage() {
        super();
    }

    public KycReminderEmailMessage(String kycLinkUri) {
        super();
        this.kycLinkUri = kycLinkUri;
    }

    public KycReminderEmailMessage(InvestorMessageDTO investor, String kycLinkUri) {
        super(investor);
        this.kycLinkUri = kycLinkUri;
    }

    public String getKycLinkUri() {
        return kycLinkUri;
    }

}
