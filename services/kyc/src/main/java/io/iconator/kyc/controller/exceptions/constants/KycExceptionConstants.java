package io.iconator.kyc.controller.exceptions.constants;

public class KycExceptionConstants {

    public final static String  UNEXPECTED_REASON = "Unexpected error occurred.";
    public final static int     UNEXPECTED_CODE = 0;

    public final static String  KYC_ALREADY_STARTED_WITH_EMAIL_REASON = "KYC was already started and KYC start email was sent.";
    public final static int     KYC_ALREADY_STARTED_WITH_EMAIL_CODE = 1;

    public final static String  KYC_ALREADY_STARTED_WITHOUT_EMAIL_REASON = "KYC was already started but KYC start email was not sent.";
    public final static int     KYC_ALREADY_STARTED_WITHOUT_EMAIL_CODE = 2;

    public final static String  KYC_ALREADY_COMPLETED_REASON = "KYC was already completed.";
    public final static int     KYC_ALREADY_COMPLETED_CODE = 3;

    public final static String  KYC_NOT_YET_STARTED_REASON = "KYC process not yet started.";
    public final static int     KYC_NOT_YET_STARTED_CODE = 4;

    public final static String  KYC_LINK_REASON = "There is something wrong with the KYC link.";
    public final static int     KYC_LINK_CODE = 5;

    public final static String  NONEXISTENT_INVESTOR_REASON = "Investor does not exist.";
    public final static int     NONEXISTENT_INVESTOR_CODE = 6;



}
