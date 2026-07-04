package com.hussain.definition_api.core.exception;

public class SchemaManagementException extends RuntimeException {

    public SchemaManagementException(String message) {
        super(message);
    }

    public SchemaManagementException(String message, Throwable cause) {
        super(message, cause);
    }
}