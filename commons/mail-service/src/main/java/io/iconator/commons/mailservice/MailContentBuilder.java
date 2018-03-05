package io.iconator.commons.mailservice;

import io.iconator.commons.bitcoin.BitcoinAddressService;
import io.iconator.commons.ethereum.EthereumAddressService;
import io.iconator.commons.model.CurrencyType;
import io.iconator.commons.model.db.Investor;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@Service
public class MailContentBuilder {

    private final static Logger LOG = LoggerFactory.getLogger(MailContentBuilder.class);

    private final TemplateEngine templateEngine;
    private final BitcoinAddressService bitcoinAddressService;
    private final EthereumAddressService ethereumAddressService;

    @Autowired
    public MailContentBuilder(TemplateEngine templateEngine,
                              BitcoinAddressService bitcoinAddressService,
                              EthereumAddressService ethereumAddressService) {
        this.templateEngine = templateEngine;
        this.bitcoinAddressService = bitcoinAddressService;
        this.ethereumAddressService = ethereumAddressService;
    }

    public void buildConfirmationEmail(Optional<MimeMessageHelper> oMessage,
                                       String confirmationEmaiLink) {
        if (oMessage.isPresent()) {
            try {
                Context context = new Context();
                context.setVariable("confirmationEmaiLink", confirmationEmaiLink);
                context.setVariable("logo", "logo");
                String html5Content = templateEngine.process("confirmation_email", context);

                oMessage.get().setText(html5Content, true);

                final InputStreamSource logoImage =
                        new ByteArrayResource(IOUtils.toByteArray(this.getClass().getResourceAsStream("/images/logo.png")));
                oMessage.get().addInline("logo", logoImage, "image/png");

            } catch (MessagingException e) {
                LOG.error("Error to add inline images to the message.");
            } catch (IOException e) {
                LOG.error("Error on finding the inline image on the resources path.");
            }
        }
    }

    public void buildSummaryEmail(Optional<MimeMessageHelper> oMessage, Optional<Investor> oInvestor) {
        if (oMessage.isPresent() && oInvestor.isPresent()) {
            try {
                Context context = new Context();
                context.setVariable("walletAddress", oInvestor.get().getWalletAddress());
                context.setVariable("payInEtherAddress", ethereumAddressService.getEthereumAddressFromPublicKey(oInvestor.get().getPayInEtherPublicKey()));
                context.setVariable("payInBitcoinAddress", bitcoinAddressService.getBitcoinAddressFromPublicKey(oInvestor.get().getPayInBitcoinPublicKey()));
                context.setVariable("refundEtherAddress", oInvestor.get().getRefundEtherAddress());
                context.setVariable("refundBitcoinAddress", oInvestor.get().getRefundBitcoinAddress());

                context.setVariable("logo", "logo");
                context.setVariable("payInEtherAddressQRCode", "payInEtherAddressQRCode");
                context.setVariable("payInBitcoinAddressQRCode", "payInBitcoinAddressQRCode");
                context.setVariable("refundEtherAddressQRCode", "refundEtherAddressQRCode");
                context.setVariable("refundBitcoinAddressQRCode", "refundBitcoinAddressQRCode");

                String html5Content = templateEngine.process("summary_email", context);

                oMessage.get().setText(html5Content, true);

                // logo:
                final InputStreamSource logoImage =
                        new ByteArrayResource(IOUtils.toByteArray(this.getClass().getResourceAsStream("/images/logo.png")));
                oMessage.get().addInline("logo", logoImage, "image/png");

                // payInEtherAddress:
                ByteArrayOutputStream payInEtherAddressQRCodeStream = QRCode
                        .from(ethereumAddressService.getEthereumAddressFromPublicKey(oInvestor.get().getPayInEtherPublicKey()))
                        .to(ImageType.PNG)
                        .withSize(265, 200)
                        .stream();
                final ByteArrayResource payInEtherAddressQRCode = new ByteArrayResource(payInEtherAddressQRCodeStream.toByteArray());
                oMessage.get().addInline("payInEtherAddressQRCode", payInEtherAddressQRCode, "image/png");

                // payInBitcoinAddress:
                ByteArrayOutputStream payInBitcoinAddressQRCodeStream = QRCode
                        .from(bitcoinAddressService.getBitcoinAddressFromPublicKey(oInvestor.get().getPayInBitcoinPublicKey()))
                        .to(ImageType.PNG)
                        .withSize(265, 200)
                        .stream();
                final ByteArrayResource payInBitcoinQRCodeImage = new ByteArrayResource(payInBitcoinAddressQRCodeStream.toByteArray());
                oMessage.get().addInline("payInBitcoinAddressQRCode", payInBitcoinQRCodeImage, "image/png");

            } catch (MessagingException e) {
                LOG.error("Error to add inline images to the message.");
            } catch (IOException e) {
                LOG.error("Error on finding the inline image on the resources path.");
            }
        }
    }

    public void buildFundsReceivedEmail(Optional<MimeMessageHelper> oMessage,
                                        BigDecimal amountFundsReceived, CurrencyType currencyType,
                                        String link, BigDecimal amountTokens, String tokenSymbol) {
        if (oMessage.isPresent()) {
            try {
                Context context = new Context();
                context.setVariable("amountFundsReceived", amountFundsReceived);
                context.setVariable("currencyFundsReceived", currencyType.name());
                context.setVariable("link", link);
                context.setVariable("amountTokens", amountTokens);
                context.setVariable("tokenSymbol", tokenSymbol);
                String html5Content = templateEngine.process("funds_received_email", context);
                oMessage.get().setText(html5Content, true);

                final InputStreamSource logoImage =
                        new ByteArrayResource(IOUtils.toByteArray(this.getClass().getResourceAsStream(
                                "/images/logo.png")));

                oMessage.get().addInline("logo", logoImage, "image/png");
            } catch (MessagingException e) {
                LOG.error("Error to add inline images to the message.");
            } catch (IOException e) {
                LOG.error("Error on finding the inline image on the resources path.");
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

}
