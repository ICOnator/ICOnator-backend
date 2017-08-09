package io.modum.tokenapp.backend.tasks;

import io.modum.tokenapp.backend.service.FileQueueService;
import io.modum.tokenapp.backend.service.MailService;
import io.modum.tokenapp.backend.service.exceptions.BaseEmailException;
import io.modum.tokenapp.backend.service.model.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SendEmailTask {

    private static final Logger LOG = LoggerFactory.getLogger(SendEmailTask.class);

    @Value("${modum.tokenapp.email.max-times-requeued}")
    private int maxReQueued;

    @Autowired
    private FileQueueService fileQueueService;

    @Autowired
    private MailService mailService;

    public SendEmailTask() {
    }

    @Scheduled(initialDelay = 10000, fixedRateString = "${modum.tokenapp.email.send-email-interval}")
    public void sendEmail() {
        Optional<Email> oEmail = Optional.empty();
        try {
            oEmail = this.fileQueueService.peekEmail();
            if (oEmail.isPresent()) {
                Email email = oEmail.get();
                switch (email.getMailType()) {
                    case CONFIRMATION_EMAIL:
                        this.mailService.sendConfirmationEmail(email.getInvestor(), email.getConfirmationEmaiLink().toString());
                        break;
                    case SUMMARY_EMAIL:
                        this.mailService.sendSummaryEmail(email.getInvestor());
                        break;
                    default:
                        break;
                }
            }
        } catch (BaseEmailException e) {
            Email email = oEmail.get();
            if (email.getReQueued() < this.maxReQueued) {
                LOG.error("Email was supposed to be sent, but an exception happened and it got re-queued.", e);
                email.setReQueued(email.getReQueued() + 1);
                this.fileQueueService.addEmail(email);
            }
        }
    }

}
