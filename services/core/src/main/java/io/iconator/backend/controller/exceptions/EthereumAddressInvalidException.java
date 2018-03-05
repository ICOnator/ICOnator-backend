package io.iconator.backend.controller.exceptions;

import io.iconator.backend.controller.exceptions.constants.ExceptionConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = ExceptionConstants.ETHEREUM_ADDRESS_INVALID_REASON)
public class EthereumAddressInvalidException extends BaseException {

    public EthereumAddressInvalidException() {
        super(ExceptionConstants.ETHEREUM_ADDRESS_INVALID_CODE);
    }
}
