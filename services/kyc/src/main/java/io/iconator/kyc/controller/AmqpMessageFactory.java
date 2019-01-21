package io.iconator.kyc.controller;

import io.iconator.commons.amqp.model.KycReminderEmailMessage;
import io.iconator.commons.amqp.model.KycStartEmailMessage;
import io.iconator.commons.model.db.Investor;

import java.net.URI;

import static io.iconator.commons.amqp.model.utils.MessageDTOHelper.build;

/**
 * Helper class for creating AMQP messages enabling simpler testing
 */
public class AmqpMessageFactory {

    /**
     * @param investor The investor to whom the email should be sent
     * @param kycUri URI to start the KYC process
     * @return AMQP message used to send the KYC start email
     */
    public KycStartEmailMessage makeKycStartEmailMessage(Investor investor, URI kycUri) {
        return new KycStartEmailMessage(build(investor), kycUri.toASCIIString());
    }

    /**
     * @param investor The investor to whom the email should be sent
     * @param kycUri URI to start the KYC process
     * @return AMQP message used to send the KYC reminder email
     */
    public KycReminderEmailMessage makeKycReminderEmailMessage(Investor investor, URI kycUri) {
        return new KycReminderEmailMessage(build(investor), kycUri.toASCIIString());
    }

}
