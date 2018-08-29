package io.iconator.core.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static io.iconator.core.controller.exceptions.constants.ExceptionConstants.ETHEREUM_ADDRESS_INVALID_CODE;
import static io.iconator.core.controller.exceptions.constants.ExceptionConstants.ETHEREUM_ADDRESS_INVALID_REASON;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = ETHEREUM_ADDRESS_INVALID_REASON)
public class EthereumAddressInvalidException extends BaseException {

    public EthereumAddressInvalidException() {
        super(ETHEREUM_ADDRESS_INVALID_CODE, ETHEREUM_ADDRESS_INVALID_REASON);
    }
}
