package io.iconator.monitor.service.exceptions;

public class FxException extends Exception {

    public FxException(String message) {
        super(message);
    }

    public FxException(String message, Throwable cause) {
        super(message, cause);
    }

    public FxException(Throwable cause) {
        super(cause);
    }

    public FxException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
