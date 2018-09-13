package io.iconator.kyc.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static io.iconator.kyc.controller.exceptions.constants.KycExceptionConstants.KYC_LINK_CODE;
import static io.iconator.kyc.controller.exceptions.constants.KycExceptionConstants.KYC_LINK_REASON;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = KYC_LINK_REASON)
public class KycLinkException extends BaseException {

    public KycLinkException() {
        super(KYC_LINK_CODE, KYC_LINK_REASON);
    }
}
