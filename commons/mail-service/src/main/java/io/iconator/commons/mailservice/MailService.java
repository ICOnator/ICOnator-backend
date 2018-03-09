package io.iconator.commons.mailservice;

import io.iconator.commons.mailservice.config.MailServiceConfig;
import io.iconator.commons.mailservice.config.MailServiceConfigHolder;
import io.iconator.commons.mailservice.exceptions.EmailNotPreparedException;
import io.iconator.commons.mailservice.exceptions.EmailNotSentException;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.Investor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Properties;

@Service
@Import({MailServiceConfigHolder.class, MailServiceConfig.class})
public class MailService {

    private final static Logger LOG = LoggerFactory.getLogger(MailService.class);

    @Autowired
    private MailContentBuilder mailContentBuilder;

    @Autowired
    private JavaMailSender javaMailService;

    @Autowired
    private MailServiceConfigHolder mailServiceConfigHolder;

    public void sendConfirmationEmail(Investor investor, String confirmationEmaiLink)
            throws EmailNotSentException, EmailNotPreparedException {
        Optional<MimeMessage> oMessageContainer = createMessageContainer(investor.getEmail());
        Optional<MimeMessageHelper> oMessage = prepareMessage(oMessageContainer, investor.getEmail(),
                this.mailServiceConfigHolder.getConfirmationEmailSubject(), MailType.CONFIRMATION_EMAIL);
        this.mailContentBuilder.buildConfirmationEmail(oMessage, confirmationEmaiLink);
        if (this.mailServiceConfigHolder.isEnabled()) {
            sendMail(oMessage, MailType.CONFIRMATION_EMAIL);
        } else {
            LOG.info("Skip sending {} email to {}, link: {}", MailType.CONFIRMATION_EMAIL,
                    investor.getEmail(), confirmationEmaiLink);
        }
    }

    public void sendSummaryEmail(Investor investor) throws EmailNotSentException, EmailNotPreparedException {
        Optional<MimeMessage> oMessageContainer = createMessageContainer(investor.getEmail());
        Optional<MimeMessageHelper> oMessage = prepareMessage(oMessageContainer, investor.getEmail(),
                this.mailServiceConfigHolder.getSummaryEmailSubject(), MailType.SUMMARY_EMAIL);
        this.mailContentBuilder.buildSummaryEmail(oMessage, Optional.ofNullable(investor));
        if (this.mailServiceConfigHolder.isEnabled()) {
            sendMail(oMessage, MailType.SUMMARY_EMAIL);
        } else {
            LOG.info("Skip sending {} email to {}", MailType.SUMMARY_EMAIL,
                    investor.getEmail());
        }
    }

    public void sendFundsReceivedEmail(Investor investor, BigDecimal amountFundsReceived,
                                       CurrencyType currencyType, String linkToTransaction,
                                       BigDecimal tokenAmount)
            throws EmailNotSentException, EmailNotPreparedException {
        Optional<MimeMessage> oMessageContainer = createMessageContainer(investor.getEmail());
        Optional<MimeMessageHelper> oMessage = prepareMessage(oMessageContainer, investor.getEmail(),
                this.mailServiceConfigHolder.getFundsReceivedEmailSubject(), MailType.FUNDS_RECEIVED_EMAIL);
        this.mailContentBuilder.buildFundsReceivedEmail(oMessage, amountFundsReceived,
                currencyType, linkToTransaction, tokenAmount, this.mailServiceConfigHolder.getTokenSymbol());
        if (this.mailServiceConfigHolder.isEnabled()) {
            sendMail(oMessage, MailType.FUNDS_RECEIVED_EMAIL);
        } else {
            LOG.info("Skip sending {} email to {}, link: {}", MailType.FUNDS_RECEIVED_EMAIL,
                    investor.getEmail());
        }
    }

    public void sendAdminMail(String content) throws EmailNotSentException, EmailNotPreparedException {
        Optional<MimeMessage> oMessageContainer = createMessageContainer(this.mailServiceConfigHolder.getAdmin());
        Optional<MimeMessageHelper> oMessage = prepareMessage(oMessageContainer, this.mailServiceConfigHolder.getAdmin(),
                "ICOnator Backend: warning message", MailType.WARNING_ADMIN_EMAIL);
        this.mailContentBuilder.buildGenericWarningMail(oMessage, content);
        sendMail(oMessage, MailType.WARNING_ADMIN_EMAIL);
    }

    private void sendMail(Optional<MimeMessageHelper> oMessage, MailType emailType) throws EmailNotSentException {
        String recipient = null;
        try {
            if (oMessage.isPresent()) {
                // TODO: don't assume that the "to" email field has at least one address
                recipient = oMessage.get().getMimeMessage().getRecipients(Message.RecipientType.TO)[0].toString();
                if (!this.mailServiceConfigHolder.isEnabled()) {
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
            throw new EmailNotSentException(e);
        }
    }

    private Optional<MimeMessageHelper> prepareMessage(Optional<MimeMessage> oMimeMessage,
                                                       String recipient, String subject, MailType emailType)
            throws EmailNotPreparedException {
        Optional<MimeMessageHelper> oMessage = Optional.empty();
        try {
            if (oMimeMessage.isPresent()) {
                oMessage = Optional.ofNullable(new MimeMessageHelper(oMimeMessage.get(), true, "UTF-8"));
                if (oMessage.isPresent()) {
                    MimeMessageHelper message = oMessage.get();
                    message.setSubject(subject);
                    message.setFrom(this.mailServiceConfigHolder.getSendfrom());
                    message.setTo(recipient);
                    if ((this.mailServiceConfigHolder.isEnableBccToConfirmationEmail() && emailType.equals(MailType.CONFIRMATION_EMAIL))
                            || (this.mailServiceConfigHolder.isEnableBccToConfirmationEmail() && emailType.equals(MailType.SUMMARY_EMAIL))) {
                        message.setBcc(this.mailServiceConfigHolder.getAdmin());
                    }
                }
            }
        } catch (MessagingException e) {
            LOG.error("Error building the message of email type {} to {}", emailType, recipient);
            throw new EmailNotPreparedException(e);
        }
        return oMessage;
    }

    private Optional<MimeMessage> createMessageContainer(String recipient) {
        return Optional.ofNullable(this.javaMailService.createMimeMessage());
    }

}
