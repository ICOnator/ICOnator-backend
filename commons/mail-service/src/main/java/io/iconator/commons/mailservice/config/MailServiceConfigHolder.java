package io.iconator.commons.mailservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class MailServiceConfigHolder {

    @Value("${io.iconator.commons.mail.service.logo.url:#{null}}")
    private Optional<String> logoUrl;

    @Value("${io.iconator.commons.mail.service.logo.content-type:#{null}}")
    private Optional<String> logoContentType;

    @Value("${io.iconator.commons.mail.service.logo.width:#{null}}")
    private Optional<Integer> logoWidth;

    @Value("${io.iconator.commons.mail.service.logo.height:#{null}}")
    private Optional<Integer> logoHeight;

    @Value("${io.iconator.commons.mail.service.token-sale.name}")
    private String tokenSaleName;

    @Value("${io.iconator.commons.mail.service.footer.entity.name}")
    private String entityName;

    @Value("${io.iconator.commons.mail.service.footer.year}")
    private String year;

    @Value("${io.iconator.commons.mail.service.confirmationEmailSubject}")
    private String confirmationEmailSubject;

    @Value("${io.iconator.commons.mail.service.summaryEmailSubject}")
    private String summaryEmailSubject;

    @Value("${io.iconator.commons.mail.service.fundsReceivedEmailSubject}")
    private String fundsReceivedEmailSubject;

    @Value("${io.iconator.commons.mail.service.kycStartEmailSubject}")
    private String kycStartEmailSubject;

    @Value("${io.iconator.commons.mail.service.token-symbol}")
    private String tokenSymbol;

    @Value("${io.iconator.commons.mail.service.enabled}")
    private boolean enabled;

    @Value("${io.iconator.commons.mail.service.host}")
    private String host;

    @Value("${io.iconator.commons.mail.service.protocol}")
    private String protocol;

    @Value("${io.iconator.commons.mail.service.port}")
    private int port;

    @Value("${io.iconator.commons.mail.service.auth}")
    private boolean auth;

    @Value("${io.iconator.commons.mail.service.starttls}")
    private boolean starttls;

    @Value("${io.iconator.commons.mail.service.debug}")
    private boolean debug;

    @Value("${io.iconator.commons.mail.service.trust}")
    private String trust;

    @Value("${io.iconator.commons.mail.service.username}")
    private String username;

    @Value("${io.iconator.commons.mail.service.password}")
    private String password;

    @Value("${io.iconator.commons.mail.service.admin}")
    private String admin;

    @Value("${io.iconator.commons.mail.service.enableBccToConfirmationEmail}")
    private boolean enableBccToConfirmationEmail;

    @Value("${io.iconator.commons.mail.service.enableBccToSummaryEmail}")
    private boolean enableBccToSummaryEmail;

    @Value("${io.iconator.commons.mail.service.sendfrom}")
    private String sendfrom;

    public Optional<String> getLogoUrl() {
        return logoUrl;
    }

    public Optional<String> getLogoContentType() {
        return logoContentType;
    }

    public Optional<Integer> getLogoWidth() {
        return logoWidth;
    }

    public Optional<Integer> getLogoHeight() {
        return logoHeight;
    }

    public String getTokenSaleName() {
        return tokenSaleName;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getYear() {
        return year;
    }

    public String getConfirmationEmailSubject() {
        return confirmationEmailSubject;
    }

    public String getSummaryEmailSubject() {
        return summaryEmailSubject;
    }

    public String getFundsReceivedEmailSubject() {
        return fundsReceivedEmailSubject;
    }

    public String getKycStartEmailSubject() {
        return kycStartEmailSubject;
    }

    public String getTokenSymbol() {
        return tokenSymbol;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getHost() {
        return host;
    }

    public String getProtocol() {
        return protocol;
    }

    public int getPort() {
        return port;
    }

    public boolean isAuth() {
        return auth;
    }

    public boolean isStarttls() {
        return starttls;
    }

    public boolean isDebug() {
        return debug;
    }

    public String getTrust() {
        return trust;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAdmin() {
        return admin;
    }

    public boolean isEnableBccToConfirmationEmail() {
        return enableBccToConfirmationEmail;
    }

    public boolean isEnableBccToSummaryEmail() {
        return enableBccToSummaryEmail;
    }

    public String getSendfrom() {
        return sendfrom;
    }
}
