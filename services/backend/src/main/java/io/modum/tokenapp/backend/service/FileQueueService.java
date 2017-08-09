package io.modum.tokenapp.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.tape.QueueFile;
import io.modum.tokenapp.backend.model.Investor;
import io.modum.tokenapp.backend.service.model.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

@Service
public class FileQueueService {

    private static final Logger LOG = LoggerFactory.getLogger(FileQueueService.class);

    private String queueFilePath;

    private ObjectMapper objectMapper;

    private QueueFile queueFile;

    @Autowired
    public FileQueueService(@Value("${modum.tokenapp.email.queue-file-path}") String queueFilePath,
                            @Autowired ObjectMapper objectMapper) {
        this.queueFilePath = queueFilePath;
        this.objectMapper = objectMapper;
        createQueueFile();
    }

    public FileQueueService(String queueFilePath) {
        this(queueFilePath, null);
    }

    public FileQueueService() {
        createQueueFile();
    }

    private void createQueueFile() {
        try {
            this.queueFile = new QueueFile(new File(this.queueFilePath));
        } catch (IOException e) {
            throw new RuntimeException("Error on reading the email queue file. The queue file was correctly specified?");
        }
    }

    public QueueFile getQueueFile() {
        return this.queueFile;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public synchronized void addConfirmationEmail(Investor investor, URI confirmationEmaiLink) {
        Email email = new Email(MailType.CONFIRMATION_EMAIL, investor, confirmationEmaiLink);
        addEmail(email);
    }

    public synchronized void addSummaryEmail(Investor investor) {
        Email summaryEmail = new Email(MailType.SUMMARY_EMAIL, investor);
        addEmail(summaryEmail);
    }

    public synchronized Optional<Email> peekEmail() {
        Email email = null;
        try {
            Optional<byte[]> oObj = Optional.ofNullable(getQueueFile().peek());
            if (oObj.isPresent()) {
                email = getObjectMapper().reader().forType(Email.class).readValue(oObj.get());
                getQueueFile().remove();
            }
        } catch (IOException e) {
            LOG.error("Not possible to convert string/bytes to Email object.", e);
        } catch (Exception e) {
            LOG.error("Unexpected exception in FileQueueService: {} {}", e.getMessage(), e.getCause());
        }
        return Optional.ofNullable(email);
    }

    public synchronized void addEmail(Email email) {
        try {
            getQueueFile().add(getObjectMapper().writer().forType(Email.class).writeValueAsBytes(email));
        } catch (IOException e) {
            LOG.error("Not possible to convert Email object to string/bytes: email {}, reason {}",
                    email.getInvestor().getEmail(), e);
        }
    }

}
