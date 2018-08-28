package io.iconator.commons.db.services.exception;

public class RefundEntryAlreadyExistsException extends Exception {
    private static String REASON = "Refund entry with that transaction ID " +
            "already exists in database.";

    public RefundEntryAlreadyExistsException() {
        super(REASON);
    }

    public RefundEntryAlreadyExistsException(Throwable cause) {
        super(REASON, cause);
    }
}

