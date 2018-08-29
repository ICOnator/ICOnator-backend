package io.iconator.core.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static io.iconator.core.controller.exceptions.constants.ExceptionConstants.UNEXPECTED_CODE;
import static io.iconator.core.controller.exceptions.constants.ExceptionConstants.UNEXPECTED_REASON;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = UNEXPECTED_REASON)
public class UnexpectedException extends BaseException {

    public UnexpectedException() {
        super(UNEXPECTED_CODE, UNEXPECTED_REASON);
    }
}
