package io.iconator.commons.mailservice;

import io.iconator.commons.bitcoin.BitcoinAddressService;
import io.iconator.commons.ethereum.EthereumAddressService;
import io.iconator.commons.mailservice.config.MailServiceConfigHolder;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.Investor;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

@Service
public class MailContentBuilder {

    private final static Logger LOG = LoggerFactory.getLogger(MailContentBuilder.class);

    private final TemplateEngine templateEngine;
    private final BitcoinAddressService bitcoinAddressService;
    private final EthereumAddressService ethereumAddressService;
    private final MailServiceConfigHolder mailServiceConfigHolder;

    private static ByteArrayResource logoContentData;

    @Autowired
    public MailContentBuilder(TemplateEngine templateEngine,
                              BitcoinAddressService bitcoinAddressService,
                              EthereumAddressService ethereumAddressService,
                              MailServiceConfigHolder mailServiceConfigHolder) {
        this.templateEngine = templateEngine;
        this.bitcoinAddressService = bitcoinAddressService;
        this.ethereumAddressService = ethereumAddressService;
        this.mailServiceConfigHolder = mailServiceConfigHolder;
    }

    @PostConstruct
    public void setLogoInMemory() throws Exception {
        // Avoid HTTP 403 from HTTP servers by setting a User Agent
        System.setProperty("http.agent", "ICOnator Client");
        URL url = this.mailServiceConfigHolder.getLogoUrl()
                .map((logoUrl) -> getLogoURL(logoUrl))
                .orElseGet(() -> getDefaultLogo());
        this.logoContentData = new ByteArrayResource(IOUtils.toByteArray(url));
    }

    public void buildConfirmationEmail(Optional<MimeMessageHelper> oMessage,
                                       String confirmationEmaiLink) {
        if (oMessage.isPresent()) {
            try {
                Context context = new Context();
                context.setVariable("logo", "logo");
                context.setVariable("logoWidth", getLogoWidth());
                context.setVariable("logoHeight", getLogoHeight());

                context.setVariable("tokenSaleName", this.mailServiceConfigHolder.getTokenSaleName());
                context.setVariable("entityName", this.mailServiceConfigHolder.getEntityName());
                context.setVariable("year", this.mailServiceConfigHolder.getYear());

                context.setVariable("confirmationEmaiLink", confirmationEmaiLink);

                String html5Content = this.templateEngine.process("confirmation_email", context);

                oMessage.get().setText(html5Content, true);

                oMessage.get().addInline("logo", this.logoContentData, getLogoContentType());

            } catch (MessagingException e) {
                LOG.error("Error to add inline images to the message.");
            }
        }
    }

    public void buildSummaryEmail(Optional<MimeMessageHelper> oMessage, Optional<Investor> oInvestor) {
        if (oMessage.isPresent() && oInvestor.isPresent()) {
            try {
                Context context = new Context();
                context.setVariable("logo", "logo");
                context.setVariable("logoWidth", getLogoWidth());
                context.setVariable("logoHeight", getLogoHeight());

                context.setVariable("tokenSaleName", this.mailServiceConfigHolder.getTokenSaleName());
                context.setVariable("entityName", this.mailServiceConfigHolder.getEntityName());
                context.setVariable("year", this.mailServiceConfigHolder.getYear());
                context.setVariable("tokenSymbol", this.mailServiceConfigHolder.getTokenSymbol());

                context.setVariable("walletAddress", oInvestor.get().getWalletAddress());
                context.setVariable("payInEtherAddress", oInvestor.get().getPayInEtherAddress());
                context.setVariable("payInBitcoinAddress", oInvestor.get().getPayInBitcoinAddress());
                context.setVariable("refundEtherAddress", oInvestor.get().getRefundEtherAddress());
                context.setVariable("refundBitcoinAddress", oInvestor.get().getRefundBitcoinAddress());

                context.setVariable("payInEtherAddressQRCode", "payInEtherAddressQRCode");
                context.setVariable("payInBitcoinAddressQRCode", "payInBitcoinAddressQRCode");
                context.setVariable("refundEtherAddressQRCode", "refundEtherAddressQRCode");
                context.setVariable("refundBitcoinAddressQRCode", "refundBitcoinAddressQRCode");

                String html5Content = this.templateEngine.process("summary_email", context);

                oMessage.get().setText(html5Content, true);

                oMessage.get().addInline("logo", this.logoContentData, getLogoContentType());

                // payInEtherAddress:
                ByteArrayOutputStream payInEtherAddressQRCodeStream = QRCode
                        .from(oInvestor.get().getPayInEtherAddress())
                        .to(ImageType.PNG)
                        .withSize(265, 200)
                        .stream();
                final ByteArrayResource payInEtherAddressQRCode = new ByteArrayResource(payInEtherAddressQRCodeStream.toByteArray());
                oMessage.get().addInline("payInEtherAddressQRCode", payInEtherAddressQRCode, "image/png");

                // payInBitcoinAddress:
                ByteArrayOutputStream payInBitcoinAddressQRCodeStream = QRCode
                        .from(oInvestor.get().getPayInBitcoinAddress())
                        .to(ImageType.PNG)
                        .withSize(265, 200)
                        .stream();
                final ByteArrayResource payInBitcoinQRCodeImage = new ByteArrayResource(payInBitcoinAddressQRCodeStream.toByteArray());
                oMessage.get().addInline("payInBitcoinAddressQRCode", payInBitcoinQRCodeImage, "image/png");

            } catch (MessagingException e) {
                LOG.error("Error to add inline images to the message.");
            }
        }
    }

    public void buildTransactionReceivedEmail(Optional<MimeMessageHelper> oMessage,
                                              BigDecimal amountFundsReceived, CurrencyType currencyType,
                                              String transactionUrl) {
        if (oMessage.isPresent()) {
            try {
                Context context = new Context();
                context.setVariable("logo", "logo");
                context.setVariable("logoWidth", getLogoWidth());
                context.setVariable("logoHeight", getLogoHeight());

                context.setVariable("amountFundsReceived", amountFundsReceived);
                context.setVariable("currencyFundsReceived", currencyType.name());
                context.setVariable("transactionUrl", transactionUrl);
                context.setVariable("tokenSymbol", this.mailServiceConfigHolder.getTokenSymbol());
                context.setVariable("entityName", this.mailServiceConfigHolder.getEntityName());
                context.setVariable("year", this.mailServiceConfigHolder.getYear());
                String html5Content = this.templateEngine.process("transaction_received_email", context);
                oMessage.get().setText(html5Content, true);

                oMessage.get().addInline("logo", this.logoContentData, getLogoContentType());

            } catch (MessagingException e) {
                LOG.error("Error to add inline images to the message.");
            }
        }
    }

    public void buildTokensAllocatedEmail(Optional<MimeMessageHelper> oMessage,
                                          BigDecimal amountFundsReceived, CurrencyType currencyType,
                                          String transactionUrl, BigDecimal tokenAmount) {
        if (oMessage.isPresent()) {
            try {
                Context context = new Context();
                context.setVariable("logo", "logo");
                context.setVariable("logoWidth", getLogoWidth());
                context.setVariable("logoHeight", getLogoHeight());

                context.setVariable("amountFundsReceived", amountFundsReceived);
                context.setVariable("currencyFundsReceived", currencyType.name());
                context.setVariable("transactionUrl", transactionUrl);
                context.setVariable("tokenAmount", tokenAmount);
                context.setVariable("tokenSymbol", this.mailServiceConfigHolder.getTokenSymbol());
                context.setVariable("entityName", this.mailServiceConfigHolder.getEntityName());
                context.setVariable("year", this.mailServiceConfigHolder.getYear());
                String html5Content = this.templateEngine.process("tokens_allocated_email", context);
                oMessage.get().setText(html5Content, true);

                oMessage.get().addInline("logo", this.logoContentData, getLogoContentType());

            } catch (MessagingException e) {
                LOG.error("Error to add inline images to the message.");
            }
        }
    }

    public void buildKycStartEmail(Optional<MimeMessageHelper> oMessage,
                                   String kycUrl) {
        if (oMessage.isPresent()) {
            try {
                Context context = new Context();
                context.setVariable("logo", "logo");
                context.setVariable("logoWidth", getLogoWidth());
                context.setVariable("logoHeight", getLogoHeight());

                context.setVariable("kycUrl", kycUrl);
                context.setVariable("entityName", this.mailServiceConfigHolder.getEntityName());
                context.setVariable("year", this.mailServiceConfigHolder.getYear());
                String html5Content = this.templateEngine.process("kyc_start_email", context);
                oMessage.get().setText(html5Content, true);

                oMessage.get().addInline("logo", this.logoContentData, getLogoContentType());
            } catch (MessagingException e) {
                LOG.error("Error to add inline images to the message.");
            }
        }
    }

    public void buildKycReminderEmail(Optional<MimeMessageHelper> oMessage,
                                      String kycUrl) {
        if (oMessage.isPresent()) {
            try {
                Context context = new Context();
                context.setVariable("logo", "logo");
                context.setVariable("logoWidth", getLogoWidth());
                context.setVariable("logoHeight", getLogoHeight());

                context.setVariable("kycUrl", kycUrl);
                LOG.debug(kycUrl);
                context.setVariable("entityName", this.mailServiceConfigHolder.getEntityName());
                context.setVariable("year", this.mailServiceConfigHolder.getYear());
                String html5Content = this.templateEngine.process("kyc_reminder_email", context);
                oMessage.get().setText(html5Content, true);

                oMessage.get().addInline("logo", this.logoContentData, getLogoContentType());
            } catch (MessagingException e) {
                LOG.error("Error to add inline images to the message.");
            }
        }
    }

    public void buildGenericWarningMail(Optional<MimeMessageHelper> oMessage, String warningContent) {
        if (oMessage.isPresent()) {
            try {
                oMessage.get().setText(warningContent, true);
            } catch (MessagingException e) {
                LOG.error("Error to add inline images to the message.");
            }
        }
    }

    private URL getLogoURL(String logoUrl) {
        try {
            return new URL(logoUrl);
        } catch (MalformedURLException e) {
            LOG.error("Error on finding the specified logo as URL.");
            return null;
        }
    }

    private URL getDefaultLogo() {
        LOG.info("Using the default ICOnator logo.");
        return this.getClass().getResource("/images/logo.png");
    }

    private String getLogoContentType() {
        return this.mailServiceConfigHolder.getLogoContentType().orElse("image/png");
    }

    private Integer getLogoWidth() {
        return this.mailServiceConfigHolder.getLogoWidth().orElse(360);
    }

    private Integer getLogoHeight() {
        return this.mailServiceConfigHolder.getLogoHeight().orElse(70);
    }

}
