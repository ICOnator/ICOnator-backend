package io.iconator.monitor.service.exceptions;

public class USDETHFxException extends Exception {

    public USDETHFxException(String message) {
        super(message);
    }

    public USDETHFxException(String message, Throwable cause) {
        super(message, cause);
    }

    public USDETHFxException(Throwable cause) {
        super(cause);
    }

    public USDETHFxException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
