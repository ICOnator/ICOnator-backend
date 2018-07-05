package io.iconator.commons.amqp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.iconator.commons.amqp.model.dto.InvestorMessageDTO;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KycStartEmailMessage extends IncludeInvestorMessage {

    private String kycLinkUri;

    public KycStartEmailMessage() {
        super();
    }

    public KycStartEmailMessage(String kycLinkUri) {
        super();
        this.kycLinkUri = kycLinkUri;
    }

    public KycStartEmailMessage(InvestorMessageDTO investor, String kycLinkUri) {
        super(investor);
        this.kycLinkUri = kycLinkUri;
    }

    public String getKycLinkUri() {
        return kycLinkUri;
    }

}
