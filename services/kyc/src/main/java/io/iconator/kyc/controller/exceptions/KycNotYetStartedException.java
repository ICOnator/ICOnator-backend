package io.iconator.kyc.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static io.iconator.kyc.controller.exceptions.constants.KycExceptionConstants.KYC_NOT_YET_STARTED_CODE;
import static io.iconator.kyc.controller.exceptions.constants.KycExceptionConstants.KYC_NOT_YET_STARTED_REASON;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = KYC_NOT_YET_STARTED_REASON)
public class KycNotYetStartedException extends BaseException {

    public KycNotYetStartedException() {
        super(KYC_NOT_YET_STARTED_CODE, KYC_NOT_YET_STARTED_REASON);
    }
}
