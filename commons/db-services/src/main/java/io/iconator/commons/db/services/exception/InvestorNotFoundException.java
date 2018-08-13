package io.iconator.commons.db.services.exception;

public class InvestorNotFoundException extends Exception {

    private static String REASON = "Investor not found in database.";

    public InvestorNotFoundException() {
        super(REASON);
    }

    public InvestorNotFoundException(Throwable cause) {
        super(REASON, cause);
    }
}
