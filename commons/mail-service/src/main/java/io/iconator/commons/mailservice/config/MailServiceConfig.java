package io.iconator.commons.mailservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@Import(MailServiceConfigHolder.class)
public class MailServiceConfig {

    @Bean
    public JavaMailSender javaMailService(MailServiceConfigHolder mailServiceConfigHolder) {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

        if (mailServiceConfigHolder.isAuth()) {
            javaMailSender.setUsername(mailServiceConfigHolder.getUsername());
            javaMailSender.setPassword(mailServiceConfigHolder.getPassword());
        }

        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", mailServiceConfigHolder.getProtocol());
        properties.setProperty("mail.smtp.auth", Boolean.toString(mailServiceConfigHolder.isAuth()));
        properties.setProperty("mail.smtp.starttls.enable", Boolean.toString(mailServiceConfigHolder.isStarttls()));
        properties.setProperty("mail.debug", Boolean.toString(mailServiceConfigHolder.isDebug()));
        properties.setProperty("mail.smtp.host", mailServiceConfigHolder.getHost());
        properties.setProperty("mail.smtp.port", Integer.toString(mailServiceConfigHolder.getPort()));
        properties.setProperty("mail.smtp.ssl.trust", mailServiceConfigHolder.getTrust());
        javaMailSender.setJavaMailProperties(properties);

        return javaMailSender;
    }

}
