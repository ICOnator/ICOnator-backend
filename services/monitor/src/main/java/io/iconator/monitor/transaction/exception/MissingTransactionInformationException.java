package io.iconator.monitor.transaction.exception;

public class MissingTransactionInformationException extends Exception {
    public MissingTransactionInformationException(String message) {
        super(message);
    }

    public MissingTransactionInformationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingTransactionInformationException(Throwable cause) {
        super(cause);
    }

    public MissingTransactionInformationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
