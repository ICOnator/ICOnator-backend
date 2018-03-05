package io.iconator.commons.amqp.service;

import io.iconator.commons.amqp.model.ConfirmationEmailMessage;
import io.iconator.commons.amqp.model.FundsReceivedEmailMessage;
import io.iconator.commons.amqp.model.SummaryEmailMessage;

public interface ICOnatorMessageService {

    void send(ConfirmationEmailMessage confirmationEmailMessage);

    void send(SummaryEmailMessage summaryEmailMessage);

    void send(FundsReceivedEmailMessage fundsReceivedEmailMessage);

}
