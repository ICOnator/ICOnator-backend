package io.iconator.commons.recaptcha.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecaptchaConfigHolder {

    @Value("${io.iconator.commons.recaptcha.enabled}")
    private boolean enabled;

    @Value("${io.iconator.commons.recaptcha.secret-key}")
    private String secretKey;

    public boolean isEnabled() {
        return enabled;
    }

    public String getSecretKey() {
        return secretKey;
    }
}
