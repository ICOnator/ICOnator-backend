package io.modum.tokenapp.backend.controller.exceptions.constants;

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

}
