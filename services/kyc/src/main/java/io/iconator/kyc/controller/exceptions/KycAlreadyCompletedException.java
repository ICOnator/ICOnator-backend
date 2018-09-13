package io.iconator.kyc.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static io.iconator.kyc.controller.exceptions.constants.KycExceptionConstants.KYC_ALREADY_COMPLETED_CODE;
import static io.iconator.kyc.controller.exceptions.constants.KycExceptionConstants.KYC_ALREADY_COMPLETED_REASON;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = KYC_ALREADY_COMPLETED_REASON)
public class KycAlreadyCompletedException extends BaseException {

    public KycAlreadyCompletedException() {
        super(KYC_ALREADY_COMPLETED_CODE, KYC_ALREADY_COMPLETED_REASON);
    }
}
