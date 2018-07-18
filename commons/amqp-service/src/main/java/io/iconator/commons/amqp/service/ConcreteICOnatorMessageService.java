package io.iconator.commons.amqp.service;

import io.iconator.commons.amqp.AMQPMessageService;
import io.iconator.commons.amqp.model.*;
import org.slf4j.Logger;

import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.*;
import static org.slf4j.LoggerFactory.getLogger;

public class ConcreteICOnatorMessageService implements ICOnatorMessageService {

    private static final Logger LOG = getLogger(ConcreteICOnatorMessageService.class);

    private final AMQPMessageService amqpMessageService;

    public ConcreteICOnatorMessageService(AMQPMessageService amqpMessageService) {
        this.amqpMessageService = amqpMessageService;
    }

    @Override
    public void send(ConfirmationEmailMessage confirmationEmailMessage) {
        sendExchange(REGISTER_CONFIRMATION_EMAIL_ROUTING_KEY, confirmationEmailMessage);
    }

    @Override
    public void send(SummaryEmailMessage summaryEmailMessage) {
        sendExchange(REGISTER_SUMMARY_EMAIL_ROUTING_KEY, summaryEmailMessage);
    }

    @Override
    public void send(FundsReceivedEmailMessage fundsReceivedEmailMessage) {
        sendExchange(FUNDS_RECEIVED_ROUTING_KEY, fundsReceivedEmailMessage);
    }

    @Override
    public void send(SetWalletAddressMessage setWalletAddressMessage) {
        sendExchange(ADDRESS_SET_WALLET_ROUTING_KEY, setWalletAddressMessage);
    }

    @Override
    public void send(KycStartEmailMessage kycStartEmailMessage) {
        sendExchange(KYC_START_EMAIL_ROUTING_KEY, kycStartEmailMessage);
    }

    @Override
    public void send(KycReminderEmailMessage kycReminderEmailMessage) {
        sendExchange(KYC_REMINDER_EMAIL_ROUTING_KEY, kycReminderEmailMessage);
    }

    @Override
    public void send(KycStartEmailSentMessage kycStartEmailSentMessage) {
        sendExchange(KYC_START_EMAIL_SENT_ROUTING_KEY, kycStartEmailSentMessage);
    }

    @Override
    public void send(KycReminderEmailSentMessage kycReminderEmailSentMessage) {
        sendExchange(KYC_REMINDER_EMAIL_SENT_ROUTING_KEY, kycReminderEmailSentMessage);
    }

    private void sendExchange(String routingKey, Object message) {
        this.amqpMessageService.send(routingKey, message);
        LOG.info("Message to <{}> exchange. Message content: {}", routingKey, message);
    }

}
