package com.hussain.data_api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    private String id;
    private String transactionId;
    private String operation;
    private String entity;
    private String userId;
    private String ipAddress;
    private Map<String, Object> requestData;
    private Map<String, Object> responseData;
    private LocalDateTime timestamp;
    private long executionTimeMs;
    private String status;
    private String errorMessage;
}