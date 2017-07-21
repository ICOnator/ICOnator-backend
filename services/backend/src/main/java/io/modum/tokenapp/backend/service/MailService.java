package io.modum.tokenapp.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class MailService {

    private final static Logger LOG = LoggerFactory.getLogger(MailService.class);

    @Autowired
    private MailContentBuilder mailContentBuilder;

    @Value("${modum.tokenapp.email.confirmationEmailSubject}")
    private String confirmationEmailSubject;

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

    public void sendConfirmationEmail(String recipient, String url) {
        final String content = this.mailContentBuilder.buildConfirmationEmail(url);
        sendMail(recipient, confirmationEmailSubject, MailType.CONFIRMATION_EMAIL, content);
    }

//    public void sendAdminMail(String subject, String content) {
//        sendMail(this.admin, subject, content);
//    }

    private void sendMail(String recipient, String subject, MailType emailType, String content) {
        if (!this.enabled) {
            LOG.info("Skipping sending email type {} to {} with body: \"{}\"", emailType, recipient, content);
            return;
        }

        // Prepare message using a Spring helper
        final MimeMessage mimeMessage = this.javaMailService.createMimeMessage();
        final MimeMessageHelper message; // true = multipart
        try {
            message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            message.setSubject(subject);
            message.setFrom(this.sendfrom);
            message.setTo(recipient);
            message.setText(content, true); // true = isHtml
        } catch (MessagingException e) {
            LOG.error("\"Error sending email type {} to {} with body: \"{}\"", emailType, recipient, content);
        }

        // Add the inline image, referenced from the HTML code as "cid:${imageResourceName}"
        // final InputStreamSource imageSource = new ByteArrayResource(imageBytes);
        // message.addInline(imageResourceName, imageSource, imageContentType);


        try {
            LOG.info("Sending email type {} to {}", emailType, recipient);
            javaMailService.send(mimeMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
