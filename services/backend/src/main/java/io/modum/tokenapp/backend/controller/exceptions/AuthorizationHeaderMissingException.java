package io.modum.tokenapp.backend.controller.exceptions;

import io.modum.tokenapp.backend.controller.exceptions.constants.ExceptionConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.OK, reason = ExceptionConstants.AUTHORIZATION_HEADER_MISSING_REASON)
public class AuthorizationHeaderMissingException extends BaseException {

    public AuthorizationHeaderMissingException() {
        super(ExceptionConstants.AUTHORIZATION_HEADER_MISSING_CODE);
    }

}
