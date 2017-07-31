package io.modum.tokenapp.backend.service;

import io.modum.tokenapp.backend.model.Investor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Optional;
import java.util.Properties;

@Service
public class MailService {

    private final static Logger LOG = LoggerFactory.getLogger(MailService.class);

    @Autowired
    private MailContentBuilder mailContentBuilder;

    @Value("${modum.tokenapp.email.confirmationEmailSubject}")
    private String confirmationEmailSubject;

    @Value("${modum.tokenapp.email.summaryEmailSubject}")
    private String summaryEmailSubject;

    @Value("${modum.tokenapp.email.enabled}")
    private boolean enabled;

    @Value("${modum.tokenapp.email.host}")
    private String host;

    @Value("${modum.tokenapp.email.protocol}")
    private String protocol;

    @Value("${modum.tokenapp.email.port}")
    private int port;

    @Value("${modum.tokenapp.email.auth}")
    private boolean auth;

    @Value("${modum.tokenapp.email.starttls}")
    private boolean starttls;

    @Value("${modum.tokenapp.email.debug}")
    private boolean debug;

    @Value("${modum.tokenapp.email.trust}")
    private String trust;

    @Value("${modum.tokenapp.email.username}")
    private String username;

    @Value("${modum.tokenapp.email.password}")
    private String password;

    @Value("${modum.tokenapp.email.admin}")
    private String admin;

    @Value("${modum.tokenapp.email.enableBccToConfirmationEmail}")
    private boolean enableBccToConfirmationEmail;

    @Value("${modum.tokenapp.email.enableBccToSummaryEmail}")
    private boolean enableBccToSummaryEmail;

    @Value("${modum.tokenapp.email.sendfrom}")
    private String sendfrom;

    @Autowired
    private JavaMailSender javaMailService;

    @Bean
    public JavaMailSender javaMailService() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

        if (this.auth) {
            javaMailSender.setUsername(this.username);
            javaMailSender.setPassword(this.password);
        }

        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", this.protocol);
        properties.setProperty("mail.smtp.auth", Boolean.toString(this.auth));
        properties.setProperty("mail.smtp.starttls.enable", Boolean.toString(this.starttls));
        properties.setProperty("mail.debug", Boolean.toString(this.debug));
        properties.setProperty("mail.smtp.host", this.host);
        properties.setProperty("mail.smtp.port", Integer.toString(this.port));
        properties.setProperty("mail.smtp.ssl.trust", this.trust);
        javaMailSender.setJavaMailProperties(properties);

        return javaMailSender;
    }

    public void sendConfirmationEmail(Investor investor, String confirmationEmaiLink) {
        Optional<MimeMessage> oMessageContainer = createMessageContainer(investor.getEmail());
        Optional<MimeMessageHelper> oMessage = prepareMessage(oMessageContainer, investor.getEmail(), confirmationEmailSubject, MailType.CONFIRMATION_EMAIL);
        this.mailContentBuilder.buildConfirmationEmail(oMessage, confirmationEmaiLink);
        sendMail(oMessage, MailType.CONFIRMATION_EMAIL);
    }

    public void sendSummaryEmail(Investor investor) {
        Optional<MimeMessage> oMessageContainer = createMessageContainer(investor.getEmail());
        Optional<MimeMessageHelper> oMessage = prepareMessage(oMessageContainer, investor.getEmail(), summaryEmailSubject, MailType.SUMMARY_EMAIL);
        this.mailContentBuilder.buildSummaryEmail(oMessage, Optional.ofNullable(investor));
        sendMail(oMessage, MailType.SUMMARY_EMAIL);
    }

    public void sendAdminMail(String content) {
        Optional<MimeMessage> oMessageContainer = createMessageContainer(this.admin);
        Optional<MimeMessageHelper> oMessage = prepareMessage(oMessageContainer, this.admin, "ICO Backend: warning message", MailType.WARNING_ADMIN_EMAIL);
        this.mailContentBuilder.buildGenericWarningMail(oMessage, content);
        sendMail(oMessage, MailType.WARNING_ADMIN_EMAIL);
    }

    private void sendMail(Optional<MimeMessageHelper> oMessage, MailType emailType) {
        String recipient = null;
        try {
            if (oMessage.isPresent()) {
                // TODO: don't assume that the "to" email field has at least one address
                recipient = oMessage.get().getMimeMessage().getRecipients(Message.RecipientType.TO)[0].toString();
                if (!this.enabled) {
                    LOG.info("Skipping sending email type {} to {} with body: {}",
                            emailType, recipient, oMessage.get().getMimeMessage().getContent());
                    return;
                }
                LOG.info("Sending email type {} to {}", emailType, recipient);
                this.javaMailService.send(oMessage.get().getMimeMessage());
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            LOG.error("CRITICAL: error sending email type {} to {}. Reason {}", emailType, recipient, e.toString());
        }
    }

    private Optional<MimeMessageHelper> prepareMessage(Optional<MimeMessage> oMimeMessage,
                                                       String recipient, String subject, MailType emailType) {
        Optional<MimeMessageHelper> oMessage = Optional.empty();
        try {
            if (oMimeMessage.isPresent()) {
                oMessage = Optional.ofNullable(new MimeMessageHelper(oMimeMessage.get(), true, "UTF-8"));
                if (oMessage.isPresent()) {
                    MimeMessageHelper message = oMessage.get();
                    message.setSubject(subject);
                    message.setFrom(this.sendfrom);
                    message.setTo(recipient);
                    if ((this.enableBccToConfirmationEmail && emailType.equals(MailType.CONFIRMATION_EMAIL))
                            || (this.enableBccToSummaryEmail && emailType.equals(MailType.SUMMARY_EMAIL))) {
                        message.setBcc(this.admin);
                    }
                }
            }
        } catch (MessagingException e) {
            LOG.error("\"Error building the message of email type {} to {}", emailType, recipient);
        }
        return oMessage;
    }

    private Optional<MimeMessage> createMessageContainer(String recipient) {
        return Optional.ofNullable(this.javaMailService.createMimeMessage());
    }

}
