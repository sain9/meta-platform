package com.hussain.data_api_client.exception;

import io.grpc.Status;

public class DataApiClientException extends RuntimeException {
    private final Status.Code statusCode;

    public DataApiClientException(Status.Code statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public Status.Code getStatusCode() { return statusCode; }
}