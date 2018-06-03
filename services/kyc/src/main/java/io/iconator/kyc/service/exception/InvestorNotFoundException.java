package io.iconator.kyc.service.exception;

public class InvestorNotFoundException extends Exception {

    private static String REASON = "Investor not found in database.";

    public InvestorNotFoundException() {
        super(REASON);
    }

    public InvestorNotFoundException(Throwable cause) {
        super(REASON, cause);
    }
}
