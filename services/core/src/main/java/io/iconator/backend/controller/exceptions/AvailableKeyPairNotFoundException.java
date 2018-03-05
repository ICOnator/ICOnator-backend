package io.iconator.backend.controller.exceptions;

import io.iconator.backend.controller.exceptions.constants.ExceptionConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = ExceptionConstants.AVAILABLE_KEY_PAIR_NOT_FOUND_REASON)
public class AvailableKeyPairNotFoundException extends BaseException {

    public AvailableKeyPairNotFoundException() {
        super(ExceptionConstants.AVAILABLE_KEY_PAIR_NOT_FOUND_CODE);
    }
}
