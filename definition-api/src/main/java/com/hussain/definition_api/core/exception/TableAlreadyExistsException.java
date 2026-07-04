package com.hussain.definition_api.core.exception;

public class TableAlreadyExistsException extends RuntimeException {

    public TableAlreadyExistsException(String message) {
        super(message);
    }

    public TableAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}