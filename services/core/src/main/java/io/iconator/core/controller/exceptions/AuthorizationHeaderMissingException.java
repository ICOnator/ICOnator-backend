package io.iconator.core.controller.exceptions;

import io.iconator.core.controller.exceptions.constants.ExceptionConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = ExceptionConstants.AUTHORIZATION_HEADER_MISSING_REASON)
public class AuthorizationHeaderMissingException extends BaseException {

    public AuthorizationHeaderMissingException() {
        super(ExceptionConstants.AUTHORIZATION_HEADER_MISSING_CODE);
    }

}
