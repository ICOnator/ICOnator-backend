package io.iconator.core.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static io.iconator.core.controller.exceptions.constants.ExceptionConstants.AUTHORIZATION_HEADER_MISSING_CODE;
import static io.iconator.core.controller.exceptions.constants.ExceptionConstants.AUTHORIZATION_HEADER_MISSING_REASON;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = AUTHORIZATION_HEADER_MISSING_REASON)
public class AuthorizationHeaderMissingException extends BaseException {

    public AuthorizationHeaderMissingException() {
        super(AUTHORIZATION_HEADER_MISSING_CODE, AUTHORIZATION_HEADER_MISSING_REASON);
    }

}
