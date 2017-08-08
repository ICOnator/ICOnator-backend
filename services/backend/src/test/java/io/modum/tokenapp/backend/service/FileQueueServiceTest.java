package io.modum.tokenapp.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modum.tokenapp.backend.model.Investor;
import io.modum.tokenapp.backend.service.model.Email;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
public class FileQueueServiceTest {

    private static final String INVESTOR_EMAIL = "email@email.com";
    private static final String CONFIRMATION_EMAIL_URI = "https://localhost/test/blah";

    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    public void testFileQueue() throws IOException {
        FileQueueService fileQueueService = new FileQueueService(getTempFilePath());
        fileQueueService.getQueueFile().add("test".getBytes());
        assertThat(fileQueueService.getQueueFile().peek(), is("test".getBytes()));
    }

    @Test
    public void testFileQueueRecovering() throws IOException {
        String tempFile = getTempFilePath();

        // instantiate first file queue service instance
        FileQueueService fileQueueService1 = new FileQueueService(tempFile);
        fileQueueService1.getQueueFile().add("test".getBytes());
        // simulate that we will re-instantiate the fileQueueService after a crash,
        // with the same file...
        FileQueueService fileQueueService2 = new FileQueueService(tempFile);

        assertThat(fileQueueService2.getQueueFile().peek(), is("test".getBytes()));
    }

    @Test
    public void testAddAndPeekConfirmationEmail() throws IOException, URISyntaxException {
        FileQueueService fileQueueService = new FileQueueService(getTempFilePath(), objectMapper);
        fileQueueService.addConfirmationEmail(new Investor().setEmail(INVESTOR_EMAIL),
                new URI(CONFIRMATION_EMAIL_URI));
        Email email = fileQueueService.peekEmail().get();
        assertThat(email.getInvestor().getEmail(), is(INVESTOR_EMAIL));
        assertThat(email.getConfirmationEmaiLink().toASCIIString(), is(CONFIRMATION_EMAIL_URI));
    }

    @Test
    public void testAddAndPeekSummaryEmail() throws IOException, URISyntaxException {
        FileQueueService fileQueueService = new FileQueueService(getTempFilePath(), objectMapper);
        fileQueueService.addSummaryEmail(new Investor().setEmail(INVESTOR_EMAIL));
        Email email = fileQueueService.peekEmail().get();
        assertThat(email.getInvestor().getEmail(), is(INVESTOR_EMAIL));
    }

    private String getTempFilePath() throws IOException {
        File f = File.createTempFile("temp","email.queue");
        String filename = f.getAbsolutePath();
        f.delete();
        return filename;
    }

}
