package io.iconator.monitor.utils;

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
import io.iconator.commons.amqp.service.ICOnatorMessageService;
import io.iconator.commons.amqp.service.exceptions.InvalidMessageFormatException;

import java.util.ArrayList;
import java.util.List;

public class MockICOnatorMessageService implements ICOnatorMessageService {

    private List<ConfirmationEmailMessage> confirmationEmailMessages = new ArrayList<>();
    private List<SummaryEmailMessage> summaryEmailMessages = new ArrayList<>();
    private List<SetWalletAddressMessage> newPayInAddressesMessages = new ArrayList<>();
    private List<KycStartEmailMessage> kycStartEmailMessages = new ArrayList<>();
    private List<KycStartEmailSentMessage> kycStartEmailSentMessages = new ArrayList<>();
    private List<KycReminderEmailMessage> kycReminderEmailMessages = new ArrayList<>();
    private List<BlockNRBitcoinMessage> blockNRBitcoinMessages = new ArrayList<>();
    private List<BlockNREthereumMessage> blockNREthereumMessages = new ArrayList<>();
    private List<KycReminderEmailSentMessage> kycReminderEmailSentMessages = new ArrayList<>();
    private List<TransactionReceivedEmailMessage> transactionReceivedEmailMessages = new ArrayList<>();
    private List<TokensAllocatedEmailMessage> tokensAllocatedEmailMessages = new ArrayList<>();
    private List<FetchRatesRequestMessage> fetchRatesRequestMessages = new ArrayList<>();

    @Override
    public void send(ConfirmationEmailMessage confirmationEmailMessage) {
        confirmationEmailMessages.add(confirmationEmailMessage);
    }

    @Override
    public void send(SummaryEmailMessage summaryEmailMessage) {
        summaryEmailMessages.add(summaryEmailMessage);
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

    @Override
    public void send(BlockNrMessage blockNrMessage) {
        if (blockNrMessage instanceof BlockNREthereumMessage) {
            blockNREthereumMessages.add((BlockNREthereumMessage) blockNrMessage);
            return;
        }
        if (blockNrMessage instanceof BlockNRBitcoinMessage) {
            blockNRBitcoinMessages.add((BlockNRBitcoinMessage) blockNrMessage);
            return;
        }
    }

    @Override
    public void send(TransactionReceivedEmailMessage transactionReceivedEmailMessage) {
        transactionReceivedEmailMessages.add(transactionReceivedEmailMessage);
    }

    @Override
    public void send(TokensAllocatedEmailMessage tokensAllocatedEmailMessage) {
        tokensAllocatedEmailMessages.add(tokensAllocatedEmailMessage);
    }

    @Override
    public FetchRatesResponseMessage sendAndReceive(FetchRatesRequestMessage fetchRatesRequestMessage) throws InvalidMessageFormatException {
        fetchRatesRequestMessages.add(fetchRatesRequestMessage);
        return new FetchRatesResponseMessage();
    }

    public List<ConfirmationEmailMessage> getConfirmationEmailMessages() {
        return confirmationEmailMessages;
    }

    public List<SummaryEmailMessage> getSummaryEmailMessages() {
        return summaryEmailMessages;
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

    public List<BlockNRBitcoinMessage> getBlockNRBitcoinMessages() {
        return blockNRBitcoinMessages;
    }

    public List<BlockNREthereumMessage> getBlockNREthereumMessages() {
        return blockNREthereumMessages;
    }

    public List<KycReminderEmailSentMessage> getKycReminderEmailSentMessages() {
        return kycReminderEmailSentMessages;
    }

    public List<TransactionReceivedEmailMessage> getTransactionReceivedEmailMessages() {
        return transactionReceivedEmailMessages;
    }

    public List<TokensAllocatedEmailMessage> getTokensAllocatedEmailMessages() {
        return tokensAllocatedEmailMessages;
    }

    public List<FetchRatesRequestMessage> getFetchRatesRequestMessages() {
        return fetchRatesRequestMessages;
    }

}
