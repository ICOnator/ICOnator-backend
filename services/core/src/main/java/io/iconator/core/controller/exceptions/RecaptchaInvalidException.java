package io.iconator.core.controller.exceptions;

import io.iconator.core.controller.exceptions.constants.ExceptionConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = ExceptionConstants.RECAPTCHA_INVALID_REASON)
public class RecaptchaInvalidException extends BaseException {

    public RecaptchaInvalidException() {
        super(ExceptionConstants.RECAPTCHA_INVALID_CODE);
    }
}
