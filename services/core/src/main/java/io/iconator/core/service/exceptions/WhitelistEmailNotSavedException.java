package io.iconator.core.service.exceptions;

public class WhitelistEmailNotSavedException extends Exception {

    private static String REASON = "Whitelist email not saved.";

    public WhitelistEmailNotSavedException() {
        super(REASON);
    }

    public WhitelistEmailNotSavedException(Throwable cause) {
        super(REASON, cause);
    }
}
