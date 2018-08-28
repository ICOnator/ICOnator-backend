package io.iconator.rates.service.exceptions;

public class RateNotFoundException extends Exception {

    public RateNotFoundException(String message) {
        super(message);
    }

    public RateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
