package io.iconator.core.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static io.iconator.core.controller.exceptions.constants.ExceptionConstants.RECAPTCHA_INVALID_CODE;
import static io.iconator.core.controller.exceptions.constants.ExceptionConstants.RECAPTCHA_INVALID_REASON;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = RECAPTCHA_INVALID_REASON)
public class RecaptchaInvalidException extends BaseException {

    public RecaptchaInvalidException() {
        super(RECAPTCHA_INVALID_CODE, RECAPTCHA_INVALID_REASON);
    }
}
