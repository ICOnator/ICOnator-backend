package io.iconator.core.controller.exceptions.constants;

public class ExceptionConstants {

    public final static String  UNEXPECTED_REASON = "Unexpected error occurred.";
    public final static int     UNEXPECTED_CODE = 0;

    public final static String  CONFIRMATION_TOKEN_NOT_FOUND_REASON = "Email confirmation token not found.";
    public final static int     CONFIRMATION_TOKEN_NOT_FOUND_CODE = 1;

    public final static String  AUTHORIZATION_HEADER_MISSING_REASON = "Missing the Authorization Header.";
    public final static int     AUTHORIZATION_HEADER_MISSING_CODE = 2;

    public final static String  BITCOIN_ADDRESS_INVALID_REASON = "Invalid Bitcoin address.";
    public final static int     BITCOIN_ADDRESS_INVALID_CODE = 3;

    public final static String  ETHEREUM_ADDRESS_INVALID_REASON = "Invalid Ethereum address.";
    public final static int     ETHEREUM_ADDRESS_INVALID_CODE = 4;

    public final static String  ETHEREUM_ADDRESS_EMPTY_REASON = "Ethereum address is empty.";
    public final static int     ETHEREUM_ADDRESS_EMPTY_CODE = 5;

    public final static String  WALLET_ADDRESS_ALREADY_SET_REASON = "Wallet token address is already set.";
    public final static int     WALLET_ADDRESS_ALREADY_SET_CODE = 6;

    public final static String  AVAILABLE_KEY_PAIR_NOT_FOUND_REASON = "Not found an available address on the key pair pool.";
    public final static int     AVAILABLE_KEY_PAIR_NOT_FOUND_CODE = 7;

    public final static String  RECAPTCHA_INVALID_REASON = "Recaptcha response code could not be validated.";
    public final static int     RECAPTCHA_INVALID_CODE = 8;

    public final static String CSV_IMPORT_FAILED_REASON = "CSV file failed to be imported.";
    public final static int    CSV_IMPORT_FAILED_CODE = 9;

}
