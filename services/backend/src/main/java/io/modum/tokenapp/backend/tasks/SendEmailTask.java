package io.modum.tokenapp.backend.tasks;

import io.modum.tokenapp.backend.service.FileQueueService;
import io.modum.tokenapp.backend.service.MailService;
import io.modum.tokenapp.backend.service.model.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SendEmailTask {

    private static final Logger LOG = LoggerFactory.getLogger(SendEmailTask.class);

    @Autowired
    private FileQueueService fileQueueService;

    @Autowired
    private MailService mailService;

    public SendEmailTask() {
    }

    @Scheduled(fixedRateString = "${modum.tokenapp.email.send-email-interval}")
    public void sendEmail() {
        // TODO: peek email and send it! :-)
        //Optional<Email> email = this.fileQueueService.peekEmail();
    }

}
