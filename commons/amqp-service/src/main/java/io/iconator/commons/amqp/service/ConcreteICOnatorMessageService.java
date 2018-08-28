package io.iconator.commons.amqp.service;

import io.iconator.commons.amqp.AMQPMessageService;
import io.iconator.commons.amqp.model.BlockNRBitcoinMessage;
import io.iconator.commons.amqp.model.BlockNREthereumMessage;
import io.iconator.commons.amqp.model.BlockNrMessage;
import io.iconator.commons.amqp.model.ConfirmationEmailMessage;
import io.iconator.commons.amqp.model.FetchRatesRequestMessage;
import io.iconator.commons.amqp.model.FetchRatesResponseMessage;
import io.iconator.commons.amqp.model.KycReminderEmailMessage;
import io.iconator.commons.amqp.model.KycReminderEmailSentMessage;
import io.iconator.commons.amqp.model.KycStartEmailMessage;
import io.iconator.commons.amqp.model.KycStartEmailSentMessage;
import io.iconator.commons.amqp.model.SetWalletAddressMessage;
import io.iconator.commons.amqp.model.SummaryEmailMessage;
import io.iconator.commons.amqp.model.TokensAllocatedEmailMessage;
import io.iconator.commons.amqp.model.TransactionReceivedEmailMessage;
import io.iconator.commons.amqp.service.exceptions.InvalidMessageFormatException;
import org.slf4j.Logger;

import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.ADDRESS_SET_WALLET_ROUTING_KEY;
import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.BLOCK_NR_BITCOIN_ROUTING_KEY;
import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.BLOCK_NR_ETHEREUM_ROUTING_KEY;
import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.KYC_REMINDER_EMAIL_ROUTING_KEY;
import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.KYC_REMINDER_EMAIL_SENT_ROUTING_KEY;
import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.KYC_START_EMAIL_ROUTING_KEY;
import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.KYC_START_EMAIL_SENT_ROUTING_KEY;
import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.RATES_EXCHANGE_REQUEST_ROUTING_KEY;
import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.REGISTER_CONFIRMATION_EMAIL_ROUTING_KEY;
import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.REGISTER_SUMMARY_EMAIL_ROUTING_KEY;
import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.TRANSACTION_RECEIVED_ROUTING_KEY;
import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.TRANSACTION_TOKENS_ALLOCATED_ROUTING_KEY;
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

    @Override
    public void send(BlockNrMessage blockNrMessage) {
        String routingKey = null;
        if (blockNrMessage instanceof BlockNRBitcoinMessage) {
            routingKey = BLOCK_NR_BITCOIN_ROUTING_KEY;
        } else if (blockNrMessage instanceof BlockNREthereumMessage) {
            routingKey = BLOCK_NR_ETHEREUM_ROUTING_KEY;
        }

        if (routingKey != null) sendExchange(routingKey, blockNrMessage);
    }

    @Override
    public void send(TransactionReceivedEmailMessage transactionReceivedEmailMessage) {
        sendExchange(TRANSACTION_RECEIVED_ROUTING_KEY, transactionReceivedEmailMessage);
    }

    @Override
    public void send(TokensAllocatedEmailMessage tokensAllocatedEmailMessage) {
        sendExchange(TRANSACTION_TOKENS_ALLOCATED_ROUTING_KEY, tokensAllocatedEmailMessage);
    }

    @Override
    public FetchRatesResponseMessage sendAndReceive(FetchRatesRequestMessage fetchRatesRequestMessage) throws InvalidMessageFormatException {
        return sendExchangeAndReceive(RATES_EXCHANGE_REQUEST_ROUTING_KEY, fetchRatesRequestMessage);
    }

    private void sendExchange(String routingKey, Object message) {
        LOG.info("Message to <{}> exchange. Message content: {}", routingKey, message);
        this.amqpMessageService.send(routingKey, message);
    }

    private FetchRatesResponseMessage sendExchangeAndReceive(String routingKey, Object message) throws InvalidMessageFormatException {
        LOG.info("Message to <{}> exchange, and wait for the response. Message content: {}", routingKey, message);
        // TODO: what if the received message cannot be cast? :-)
        try {
            return (FetchRatesResponseMessage) this.amqpMessageService.sendAndReceive(routingKey, message);
        } catch (Exception e) {
            throw new InvalidMessageFormatException("Cannot cast the received object to 'FetchRatesResponseMessage' class.", e);
        }
    }

}
