package io.iconator.core.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static io.iconator.core.controller.exceptions.constants.ExceptionConstants.AVAILABLE_KEY_PAIR_NOT_FOUND_CODE;
import static io.iconator.core.controller.exceptions.constants.ExceptionConstants.AVAILABLE_KEY_PAIR_NOT_FOUND_REASON;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = AVAILABLE_KEY_PAIR_NOT_FOUND_REASON)
public class AvailableKeyPairNotFoundException extends BaseException {

    public AvailableKeyPairNotFoundException() {
        super(AVAILABLE_KEY_PAIR_NOT_FOUND_CODE, AVAILABLE_KEY_PAIR_NOT_FOUND_REASON);
    }
}
