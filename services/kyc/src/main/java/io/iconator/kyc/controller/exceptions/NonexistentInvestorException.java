package io.iconator.kyc.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static io.iconator.kyc.controller.exceptions.constants.KycExceptionConstants.NONEXISTENT_INVESTOR_CODE;
import static io.iconator.kyc.controller.exceptions.constants.KycExceptionConstants.NONEXISTENT_INVESTOR_REASON;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = NONEXISTENT_INVESTOR_REASON)
public class NonexistentInvestorException extends BaseException {

    public NonexistentInvestorException() {
        super(NONEXISTENT_INVESTOR_CODE, NONEXISTENT_INVESTOR_REASON);
    }
}
