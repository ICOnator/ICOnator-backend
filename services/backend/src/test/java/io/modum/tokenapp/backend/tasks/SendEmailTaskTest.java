package io.modum.tokenapp.backend.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modum.tokenapp.backend.model.Investor;
import io.modum.tokenapp.backend.service.FileQueueService;
import io.modum.tokenapp.backend.service.model.Email;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SendEmailTaskTest {

    private static final String INVESTOR_EMAIL = "email@email.com";
    private static final String CONFIRMATION_EMAIL_URI = "https://localhost/test/blah";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSendEmailTask() throws IOException, URISyntaxException {
        FileQueueService fileQueueService = new FileQueueService(getTempFilePath(), objectMapper);
        fileQueueService.addConfirmationEmail(new Investor().setEmail(INVESTOR_EMAIL),
                new URI(CONFIRMATION_EMAIL_URI));
        Email email = fileQueueService.peekEmail().get();
        assertThat(email.getInvestor().getEmail(), is(INVESTOR_EMAIL));
        assertThat(email.getConfirmationEmaiLink().toASCIIString(), is(CONFIRMATION_EMAIL_URI));
    }

    private String getTempFilePath() throws IOException {
        File f = File.createTempFile("temp","email.queue");
        String filename = f.getAbsolutePath();
        f.delete();
        return filename;
    }

}
