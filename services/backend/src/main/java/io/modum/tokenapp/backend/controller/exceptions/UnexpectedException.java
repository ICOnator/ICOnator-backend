package io.modum.tokenapp.backend.controller.exceptions;

import io.modum.tokenapp.backend.controller.exceptions.constants.ExceptionConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.OK, reason = ExceptionConstants.UNEXPECTED_REASON)
public class UnexpectedException extends BaseException {

    public UnexpectedException() {
        super(ExceptionConstants.UNEXPECTED_CODE);
    }
}
