package io.iconator.email.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.amqp.model.TokensAllocatedEmailMessage;
import io.iconator.commons.mailservice.MailService;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.Investor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static io.iconator.commons.amqp.model.constants.ExchangeConstants.ICONATOR_ENTRY_EXCHANGE;
import static io.iconator.commons.amqp.model.constants.QueueConstants.TRANSACTION_TOKENS_ALLOCATED_EMAIL_QUEUE;
import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.TRANSACTION_TOKENS_ALLOCATED_ROUTING_KEY;

@Component
public class TokensAllocatedEmailMessageConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(TokensAllocatedEmailMessageConsumer.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MailService mailService;

    @RabbitListener(
            bindings = @QueueBinding(value = @Queue(value = TRANSACTION_TOKENS_ALLOCATED_EMAIL_QUEUE, autoDelete = "false"),
                    exchange = @Exchange(
                            value = ICONATOR_ENTRY_EXCHANGE,
                            type = ExchangeTypes.TOPIC,
                            ignoreDeclarationExceptions = "true",
                            durable = "true"
                    ),
                    key = TRANSACTION_TOKENS_ALLOCATED_ROUTING_KEY)
    )
    public void receiveMessage(byte[] message) {
        LOG.debug("Received from consumer: " + new String(message));

        TokensAllocatedEmailMessage messageObject = null;
        try {
            messageObject = objectMapper.reader().forType(TokensAllocatedEmailMessage.class).readValue(message);
        } catch (Exception e) {
            LOG.error("Message not valid.", e);
            throw new AmqpRejectAndDontRequeueException(
                    String.format("Message can't be mapped to the %s class.", TokensAllocatedEmailMessage.class.getTypeName()), e);
        }

        try {
            Investor investor = messageObject.getInvestor().toInvestor();
            BigDecimal amountFundsReceived = messageObject.getAmountFundsReceived();
            CurrencyType currencyType = messageObject.getCurrencyType();
            String transactionUrl = messageObject.getTransactionUrl();
            BigDecimal tokenAmount = messageObject.getTokenAmount();
            // TODO: 05.03.18 Guil:
            // Add a retry mechanism (e.g., for when the SMTP server is offline)
            mailService.sendTokensAllocatedEmail(investor, amountFundsReceived, currencyType, transactionUrl, tokenAmount);
        } catch (Exception e) {
            // TODO: 05.03.18 Guil:
            // Instead of just output the error, send to a structured log,
            // e.g., logstash or, also, send an email to an admin
            LOG.error("Email not sent.", e);
        }

    }

}
