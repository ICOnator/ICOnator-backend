package io.modum.tokenapp.backend.controller.exceptions;

import io.modum.tokenapp.backend.controller.exceptions.constants.ExceptionConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.OK, reason = ExceptionConstants.CONFIRMATION_TOKEN_NOT_FOUND_REASON)
public class ConfirmationTokenNotFoundException extends BaseException {

    public ConfirmationTokenNotFoundException() {
        super(ExceptionConstants.CONFIRMATION_TOKEN_NOT_FOUND_CODE);
    }

}
