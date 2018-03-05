package io.iconator.backend.controller.exceptions;

import io.iconator.backend.controller.exceptions.constants.ExceptionConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = ExceptionConstants.CONFIRMATION_TOKEN_NOT_FOUND_REASON)
public class ConfirmationTokenNotFoundException extends BaseException {

    public ConfirmationTokenNotFoundException() {
        super(ExceptionConstants.CONFIRMATION_TOKEN_NOT_FOUND_CODE);
    }

}
