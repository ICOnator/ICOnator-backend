package io.iconator.monitor.utils;

import io.iconator.commons.amqp.model.*;
import io.iconator.commons.amqp.service.ICOnatorMessageService;

import java.util.ArrayList;
import java.util.List;

public class MockICOnatorMessageService implements ICOnatorMessageService {

    private List<ConfirmationEmailMessage> confirmationEmailMessages = new ArrayList<>();
    private List<SummaryEmailMessage> summaryEmailMessages = new ArrayList<>();
    private List<FundsReceivedEmailMessage> fundsReceivedEmailMessages = new ArrayList<>();
    private List<SetWalletAddressMessage> newPayInAddressesMessages = new ArrayList<>();
    private List<KycStartEmailMessage> kycStartEmailMessages = new ArrayList<>();
    private List<KycStartEmailSentMessage> kycStartEmailSentMessages = new ArrayList<>();
    private List<KycReminderEmailMessage> kycReminderEmailMessages = new ArrayList<>();
    private List<KycReminderEmailSentMessage> kycReminderEmailSentMessages = new ArrayList<>();

    @Override
    public void send(ConfirmationEmailMessage confirmationEmailMessage) {
        confirmationEmailMessages.add(confirmationEmailMessage);
    }

    @Override
    public void send(SummaryEmailMessage summaryEmailMessage) {
        summaryEmailMessages.add(summaryEmailMessage);
    }

    @Override
    public void send(FundsReceivedEmailMessage fundsReceivedEmailMessage) {
        fundsReceivedEmailMessages.add(fundsReceivedEmailMessage);
    }

    @Override
    public void send(SetWalletAddressMessage newPayInAddressesMessage) {
        newPayInAddressesMessages.add(newPayInAddressesMessage);
    }

    @Override
    public void send(KycStartEmailMessage kycStartEmailMessage) {
        kycStartEmailMessages.add(kycStartEmailMessage);
    }

    @Override
    public void send(KycStartEmailSentMessage kycStartEmailSentMessage) {
        kycStartEmailSentMessages.add(kycStartEmailSentMessage);
    }

    @Override
    public void send(KycReminderEmailMessage kycReminderEmailMessage) {
        kycReminderEmailMessages.add(kycReminderEmailMessage);
    }

    @Override
    public void send(KycReminderEmailSentMessage kycReminderEmailSentMessage) {
        kycReminderEmailSentMessages.add(kycReminderEmailSentMessage);
    }

    public List<ConfirmationEmailMessage> getConfirmationEmailMessages() {
        return confirmationEmailMessages;
    }

    public List<SummaryEmailMessage> getSummaryEmailMessages() {
        return summaryEmailMessages;
    }

    public List<FundsReceivedEmailMessage> getFundsReceivedEmailMessages() {
        return fundsReceivedEmailMessages;
    }

    public List<SetWalletAddressMessage> getNewPayInAddressesMessages() {
        return newPayInAddressesMessages;
    }

    public List<KycStartEmailMessage> getKycStartEmailMessages() {
        return kycStartEmailMessages;
    }

    public List<KycStartEmailSentMessage> getKycStartEmailSentMessages() {
        return kycStartEmailSentMessages;
    }

    public List<KycReminderEmailMessage> getKycReminderEmailMessages() {
        return kycReminderEmailMessages;
    }

    public List<KycReminderEmailSentMessage> getKycReminderEmailSentMessages() {
        return kycReminderEmailSentMessages;
    }
}
