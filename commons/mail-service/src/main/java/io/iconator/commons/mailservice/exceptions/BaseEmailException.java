package io.iconator.commons.mailservice.exceptions;

public abstract class BaseEmailException extends Exception {

    public BaseEmailException() {
    }

    public BaseEmailException(String message) {
        super(message);
    }

    public BaseEmailException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseEmailException(Throwable cause) {
        super(cause);
    }

}
