package com.hussain.data_api.service;

import com.hussain.data_api.config.DataApiProperties;
import com.hussain.data_api.model.AuditLog;
import com.hussain.data_api.model.DataRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    
    private final DataApiProperties properties;
    
    public void audit(DataRequest request, Object result, String transactionId, 
                      long startTime, boolean success, String errorMessage) {
        if (!properties.isEnableAudit()) {
            return;
        }
        
        AuditLog auditLog = AuditLog.builder()
                .id(UUID.randomUUID().toString())
                .transactionId(transactionId)
                .operation(request.getOperation().name())
                .entity(request.getEntity())
                .userId(getCurrentUser())
                .ipAddress(getClientIp())
                .requestData(request.getData())
                .timestamp(LocalDateTime.now())
                .executionTimeMs(System.currentTimeMillis() - startTime)
                .status(success ? "SUCCESS" : "FAILED")
                .errorMessage(errorMessage)
                .build();
        
        // In a real implementation, you would save this to a database
        log.info("Audit log: {}", auditLog);
    }
    
    private String getCurrentUser() {
        // In a real implementation, get from SecurityContext
        return "system";
    }
    
    private String getClientIp() {
        // In a real implementation, get from HttpServletRequest
        return "127.0.0.1";
    }
}