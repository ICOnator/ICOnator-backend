package io.iconator.core.controller.exceptions;

import io.iconator.core.controller.exceptions.constants.ExceptionConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = ExceptionConstants.WALLET_ADDRESS_ALREADY_SET_REASON)
public class WalletAddressAlreadySetException extends BaseException {

    public WalletAddressAlreadySetException() {
        super(ExceptionConstants.WALLET_ADDRESS_ALREADY_SET_CODE);
    }
}
