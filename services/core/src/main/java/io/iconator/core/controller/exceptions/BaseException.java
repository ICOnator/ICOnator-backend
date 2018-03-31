package io.iconator.core.controller.exceptions;

public class BaseException extends Exception {

    private int internalErrorCode;

    public BaseException(int internalErrorCode) {
        this.internalErrorCode = internalErrorCode;
    }

    public int getInternalErrorCode() {
        return internalErrorCode;
    }

    public void setInternalErrorCode(int internalErrorCode) {
        this.internalErrorCode = internalErrorCode;
    }

}
