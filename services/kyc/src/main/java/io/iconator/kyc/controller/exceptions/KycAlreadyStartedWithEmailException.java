package io.iconator.kyc.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static io.iconator.kyc.controller.exceptions.constants.KycExceptionConstants.KYC_ALREADY_STARTED_WITH_EMAIL_CODE;
import static io.iconator.kyc.controller.exceptions.constants.KycExceptionConstants.KYC_ALREADY_STARTED_WITH_EMAIL_REASON;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = KYC_ALREADY_STARTED_WITH_EMAIL_REASON)
public class KycAlreadyStartedWithEmailException extends BaseException {

    public KycAlreadyStartedWithEmailException() {
        super(KYC_ALREADY_STARTED_WITH_EMAIL_CODE, KYC_ALREADY_STARTED_WITH_EMAIL_REASON);
    }

}
