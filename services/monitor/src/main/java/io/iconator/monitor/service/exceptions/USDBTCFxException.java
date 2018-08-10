package io.iconator.monitor.service.exceptions;

public class USDBTCFxException extends FxException {

    public USDBTCFxException(String message) {
        super(message);
    }

    public USDBTCFxException(String message, Throwable cause) {
        super(message, cause);
    }

    public USDBTCFxException(Throwable cause) {
        super(cause);
    }

    public USDBTCFxException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
