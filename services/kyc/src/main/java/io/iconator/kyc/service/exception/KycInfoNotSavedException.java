package io.iconator.kyc.service.exception;

public class KycInfoNotSavedException extends Exception {

    private static String REASON = "Kyc Info not saved.";

    public KycInfoNotSavedException() {
        super(REASON);
    }

    public KycInfoNotSavedException(Throwable cause) {
        super(REASON, cause);
    }
}
