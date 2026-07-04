package com.hussain.data_api.exception;

import lombok.Getter;

@Getter
public class DataApiException extends RuntimeException {
    private final String errorCode;
    private final int statusCode;
    
    public DataApiException(String message) {
        super(message);
        this.errorCode = "DATA_API_ERROR";
        this.statusCode = 500;
    }
    
    public DataApiException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = 500;
    }
    
    public DataApiException(String message, String errorCode, int statusCode) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
    }
}