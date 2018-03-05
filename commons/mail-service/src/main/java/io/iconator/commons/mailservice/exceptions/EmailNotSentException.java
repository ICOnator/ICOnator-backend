package io.iconator.commons.mailservice.exceptions;

public class EmailNotSentException extends BaseEmailException {

    public EmailNotSentException() {
    }

    public EmailNotSentException(String message) {
        super(message);
    }

    public EmailNotSentException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailNotSentException(Throwable cause) {
        super(cause);
    }
}
