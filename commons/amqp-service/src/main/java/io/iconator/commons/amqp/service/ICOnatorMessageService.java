package io.iconator.commons.amqp.service;

import io.iconator.commons.amqp.model.BlockNrMessage;
import io.iconator.commons.amqp.model.ConfirmationEmailMessage;
import io.iconator.commons.amqp.model.FetchRatesRequestMessage;
import io.iconator.commons.amqp.model.FetchRatesResponseMessage;
import io.iconator.commons.amqp.model.FundsReceivedEmailMessage;
import io.iconator.commons.amqp.model.KycReminderEmailMessage;
import io.iconator.commons.amqp.model.KycReminderEmailSentMessage;
import io.iconator.commons.amqp.model.KycStartEmailMessage;
import io.iconator.commons.amqp.model.KycStartEmailSentMessage;
import io.iconator.commons.amqp.model.SetWalletAddressMessage;
import io.iconator.commons.amqp.model.SummaryEmailMessage;
import io.iconator.commons.amqp.service.exceptions.InvalidMessageFormatException;

public interface ICOnatorMessageService {

    void send(ConfirmationEmailMessage confirmationEmailMessage);

    void send(SummaryEmailMessage summaryEmailMessage);

    void send(FundsReceivedEmailMessage fundsReceivedEmailMessage);

    void send(SetWalletAddressMessage newPayInAddressesMessage);

    void send(KycStartEmailMessage kycStartEmailMessage);

    void send(KycReminderEmailMessage kycReminderEmailMessage);

    void send(KycStartEmailSentMessage kycStartEmailSentMessage);

    void send(KycReminderEmailSentMessage kycReminderEmailSentMessage);

    void send(BlockNrMessage blockNrMessage);

    FetchRatesResponseMessage sendAndReceive(FetchRatesRequestMessage fetchRatesRequestMessage) throws InvalidMessageFormatException;
}
