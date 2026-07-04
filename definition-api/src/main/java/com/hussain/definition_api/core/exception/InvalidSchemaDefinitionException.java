package com.hussain.definition_api.core.exception;

public class InvalidSchemaDefinitionException extends RuntimeException {

    public InvalidSchemaDefinitionException(String message) {
        super(message);
    }

    public InvalidSchemaDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }
}