package io.iconator.email.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.iconator.commons.amqp.model.ConfirmationEmailMessage;
import io.iconator.commons.mailservice.MailService;
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

import static io.iconator.commons.amqp.model.constants.ExchangeConstants.ICONATOR_ENTRY_EXCHANGE;
import static io.iconator.commons.amqp.model.constants.QueueConstants.REGISTER_CONFIRMATION_EMAIL_QUEUE;
import static io.iconator.commons.amqp.model.constants.RoutingKeyConstants.REGISTER_CONFIRMATION_EMAIL_ROUTING_KEY;

@Component
public class ConfirmationEmailMessageConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmationEmailMessageConsumer.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MailService mailService;

    @RabbitListener(
            bindings = @QueueBinding(value = @Queue(value = REGISTER_CONFIRMATION_EMAIL_QUEUE, autoDelete = "false"),
                    exchange = @Exchange(
                            value = ICONATOR_ENTRY_EXCHANGE,
                            type = ExchangeTypes.TOPIC,
                            ignoreDeclarationExceptions = "true",
                            durable = "true"
                    ),
                    key = REGISTER_CONFIRMATION_EMAIL_ROUTING_KEY)
    )
    public void receiveMessage(byte[] message) {
        LOG.debug("Received from consumer: " + new String(message));

        ConfirmationEmailMessage messageObject = null;
        try {
            messageObject = objectMapper.reader().forType(ConfirmationEmailMessage.class).readValue(message);
        } catch (Exception e) {
            LOG.error("Message not valid.", e);
            throw new AmqpRejectAndDontRequeueException(
                    String.format("Message can't be mapped to the %s class.", ConfirmationEmailMessage.class.getTypeName()), e);
        }

        try {
            // TODO: 05.03.18 Guil:
            // Add a retry mechanism (e.g., for when the SMTP server is offline)
            mailService.sendConfirmationEmail(
                    new Investor().setEmail(messageObject.getInvestor().getEmail()),
                    messageObject.getEmailLinkUri()
            );
        } catch (Exception e) {
            // TODO: 05.03.18 Guil:
            // Instead of just output the error, send to a structured log,
            // e.g., logstash or, also, send an email to an admin
            LOG.error("Email not sent.", e);
        }

    }

}
