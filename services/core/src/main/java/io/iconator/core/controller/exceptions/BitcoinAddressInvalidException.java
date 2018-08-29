package io.iconator.core.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static io.iconator.core.controller.exceptions.constants.ExceptionConstants.BITCOIN_ADDRESS_INVALID_CODE;
import static io.iconator.core.controller.exceptions.constants.ExceptionConstants.BITCOIN_ADDRESS_INVALID_REASON;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = BITCOIN_ADDRESS_INVALID_REASON)
public class BitcoinAddressInvalidException extends BaseException {

    public BitcoinAddressInvalidException() {
        super(BITCOIN_ADDRESS_INVALID_CODE, BITCOIN_ADDRESS_INVALID_REASON);
    }
}
