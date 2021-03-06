package io.iconator.kyc.consumer;

import io.iconator.commons.amqp.model.KycStartEmailSentMessage;
import io.iconator.commons.db.services.InvestorService;
import io.iconator.commons.db.services.exception.InvestorNotFoundException;
import io.iconator.kyc.service.KycInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.iconator.commons.amqp.model.constants.ExchangeConstants.ICONATOR_ENTRY_EXCHANGE;
import static io.iconator.commons.amqp.model.constants.QueueConstants.KYC_START_EMAIL_SENT_QUEUE;
import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.KYC_START_EMAIL_SENT_ROUTING_KEY;

/**
 * Message consumer that sets kycStartEmailSent to true after the KYC start email has been sent
 */
@Component
public class KycStartEmailSentMessageConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(KycStartEmailSentMessageConsumer.class);

    @Autowired
    private InvestorService investorService;

    @Autowired
    private KycInfoService kycInfoService;

    @RabbitListener(
            bindings = @QueueBinding(value = @Queue(value = KYC_START_EMAIL_SENT_QUEUE, autoDelete = "false"),
                    exchange = @Exchange(
                            value = ICONATOR_ENTRY_EXCHANGE,
                            type = ExchangeTypes.TOPIC,
                            ignoreDeclarationExceptions = "true",
                            durable = "true"
                    ),
                    key = KYC_START_EMAIL_SENT_ROUTING_KEY)
    )
    public void receiveMessage(KycStartEmailSentMessage message) {
        try {
            long investorId = investorService.getInvestorByEmail(message.getEmailAddress()).getId();
            kycInfoService.setKycStartEmailSent(investorId);
            LOG.debug("Set kycStartEmailSent for investor with ID {} to true", investorId);
        } catch (InvestorNotFoundException e) {
            LOG.error("Investor with address {} not found", message.getEmailAddress());
        }
    }

}
