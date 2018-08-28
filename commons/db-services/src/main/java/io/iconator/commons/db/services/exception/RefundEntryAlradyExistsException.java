package io.iconator.commons.db.services.exception;

public class RefundEntryAlradyExistsException extends Exception {
    private static String REASON = "Refund entry with that transaction ID " +
            "already exists in database.";

    public RefundEntryAlradyExistsException() {
        super(REASON);
    }

    public RefundEntryAlradyExistsException(Throwable cause) {
        super(REASON, cause);
    }
}

