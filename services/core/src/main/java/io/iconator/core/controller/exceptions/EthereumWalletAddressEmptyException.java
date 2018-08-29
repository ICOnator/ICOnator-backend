package io.iconator.core.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static io.iconator.core.controller.exceptions.constants.ExceptionConstants.ETHEREUM_ADDRESS_EMPTY_CODE;
import static io.iconator.core.controller.exceptions.constants.ExceptionConstants.ETHEREUM_ADDRESS_EMPTY_REASON;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = ETHEREUM_ADDRESS_EMPTY_REASON)
public class EthereumWalletAddressEmptyException extends BaseException {

    public EthereumWalletAddressEmptyException() {
        super(ETHEREUM_ADDRESS_EMPTY_CODE, ETHEREUM_ADDRESS_EMPTY_REASON);
    }
}
