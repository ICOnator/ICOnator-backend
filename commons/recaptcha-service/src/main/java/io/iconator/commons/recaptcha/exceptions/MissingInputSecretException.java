package io.iconator.commons.recaptcha.exceptions;

public class MissingInputSecretException extends RecaptchaException {

    public MissingInputSecretException(String message) {
        super(message);
    }

}
