package io.iconator.commons.amqp.service.exceptions;

public class InvalidMessageFormatException extends Exception {

    public InvalidMessageFormatException(String message, Throwable cause) {
        super(message, cause);
    }

}
