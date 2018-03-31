package io.iconator.core.controller.exceptions;

import io.iconator.core.controller.exceptions.constants.ExceptionConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = ExceptionConstants.BITCOIN_ADDRESS_INVALID_REASON)
public class BitcoinAddressInvalidException extends BaseException {

    public BitcoinAddressInvalidException() {
        super(ExceptionConstants.BITCOIN_ADDRESS_INVALID_CODE);
    }
}
