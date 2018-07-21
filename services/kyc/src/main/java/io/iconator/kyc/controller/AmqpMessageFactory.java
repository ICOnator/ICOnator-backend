package io.iconator.kyc.controller;

import io.iconator.commons.amqp.model.KycReminderEmailMessage;
import io.iconator.commons.amqp.model.KycStartEmailMessage;
import io.iconator.commons.model.db.Investor;

import java.net.URI;

import static io.iconator.commons.amqp.model.utils.MessageDTOHelper.build;

public class AmqpMessageFactory {

    public KycStartEmailMessage makeKycStartEmailMessage(Investor investor, URI kycUri) {
        return new KycStartEmailMessage(build(investor), kycUri.toASCIIString());
    }

    public KycReminderEmailMessage makeKycReminderEmailMessage(Investor investor, URI kycUri) {
        return new KycReminderEmailMessage(build(investor), kycUri.toASCIIString());
    }

}
