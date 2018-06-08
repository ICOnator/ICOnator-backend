package io.iconator.kyc.controller;

import io.iconator.commons.amqp.model.KycStartEmailMessage;
import io.iconator.commons.model.db.Investor;

import static io.iconator.commons.amqp.model.utils.MessageDTOHelper.build;

import java.net.URI;

public class AmqpMessageFactory {

    public KycStartEmailMessage makeKycStartEmailMessage(Investor investor, URI kycUri) {
        return new KycStartEmailMessage(build(investor), kycUri.toASCIIString());
    }

}
