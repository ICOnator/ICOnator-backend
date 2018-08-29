package io.iconator.core.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static io.iconator.core.controller.exceptions.constants.ExceptionConstants.WALLET_ADDRESS_ALREADY_SET_CODE;
import static io.iconator.core.controller.exceptions.constants.ExceptionConstants.WALLET_ADDRESS_ALREADY_SET_REASON;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = WALLET_ADDRESS_ALREADY_SET_REASON)
public class WalletAddressAlreadySetException extends BaseException {

    public WalletAddressAlreadySetException() {
        super(WALLET_ADDRESS_ALREADY_SET_CODE, WALLET_ADDRESS_ALREADY_SET_REASON);
    }
}
