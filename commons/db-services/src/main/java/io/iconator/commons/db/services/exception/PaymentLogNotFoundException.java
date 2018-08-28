package io.iconator.commons.db.services.exception;

public class PaymentLogNotFoundException extends Exception {

    private static String REASON = "PaymentLog not found in database.";

    public PaymentLogNotFoundException() {
        super(REASON);
    }

    public PaymentLogNotFoundException(Throwable cause) {
        super(REASON, cause);
    }
}
