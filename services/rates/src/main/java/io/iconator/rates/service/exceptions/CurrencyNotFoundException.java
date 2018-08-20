package io.iconator.rates.service.exceptions;

public class CurrencyNotFoundException extends Exception {

    public CurrencyNotFoundException(String message) {
        super(message);
    }

    public CurrencyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
