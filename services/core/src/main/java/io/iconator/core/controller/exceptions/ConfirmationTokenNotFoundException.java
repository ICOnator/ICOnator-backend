package io.iconator.core.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static io.iconator.core.controller.exceptions.constants.ExceptionConstants.CONFIRMATION_TOKEN_NOT_FOUND_CODE;
import static io.iconator.core.controller.exceptions.constants.ExceptionConstants.CONFIRMATION_TOKEN_NOT_FOUND_REASON;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = CONFIRMATION_TOKEN_NOT_FOUND_REASON)
public class ConfirmationTokenNotFoundException extends BaseException {

    public ConfirmationTokenNotFoundException() {
        super(CONFIRMATION_TOKEN_NOT_FOUND_CODE, CONFIRMATION_TOKEN_NOT_FOUND_REASON);
    }

}
