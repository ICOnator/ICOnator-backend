package io.iconator.commons.amqp.service;

import io.iconator.commons.amqp.model.*;

public interface ICOnatorMessageService {

    void send(ConfirmationEmailMessage confirmationEmailMessage);

    void send(SummaryEmailMessage summaryEmailMessage);

    void send(FundsReceivedEmailMessage fundsReceivedEmailMessage);

    void send(SetWalletAddressMessage newPayInAddressesMessage);

    void send(KycStartEmailMessage kycStartEmailMessage);

    void send(KycReminderEmailMessage kycReminderEmailMessage);

    void send(BlockNRBitcoinMessage blockNRBitcoinMessage);

    void send(BlockNREthereumMessage blockNREthereumMessage);

    void send(KycStartEmailSentMessage kycStartEmailSentMessage);

    void send(KycReminderEmailSentMessage kycReminderEmailSentMessage);

}
