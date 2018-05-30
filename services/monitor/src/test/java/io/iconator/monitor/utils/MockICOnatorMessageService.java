package io.iconator.monitor.utils;

import io.iconator.commons.amqp.model.ConfirmationEmailMessage;
import io.iconator.commons.amqp.model.FundsReceivedEmailMessage;
import io.iconator.commons.amqp.model.SetWalletAddressMessage;
import io.iconator.commons.amqp.model.SummaryEmailMessage;
import io.iconator.commons.amqp.service.ICOnatorMessageService;

import java.util.ArrayList;
import java.util.List;

public class MockICOnatorMessageService implements ICOnatorMessageService {

    private List<ConfirmationEmailMessage> confirmationEmailMessages = new ArrayList<>();
    private List<SummaryEmailMessage> summaryEmailMessages = new ArrayList<>();
    private List<FundsReceivedEmailMessage> fundsReceivedEmailMessages = new ArrayList<>();
    private List<SetWalletAddressMessage> newPayInAddressesMessages = new ArrayList<>();

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
}
