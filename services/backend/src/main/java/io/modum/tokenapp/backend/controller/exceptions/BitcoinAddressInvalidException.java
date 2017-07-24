package io.modum.tokenapp.backend.controller.exceptions;

import io.modum.tokenapp.backend.controller.exceptions.constants.ExceptionConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.OK, reason = ExceptionConstants.BITCOIN_ADDRESS_INVALID_REASON)
public class BitcoinAddressInvalidException extends BaseException {

    public BitcoinAddressInvalidException() {
        super(ExceptionConstants.BITCOIN_ADDRESS_INVALID_CODE);
    }
}
