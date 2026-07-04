package com.hussain.definition_api.infrastructure.audit;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Jacksonized
public class SchemaAuditEvent {
    private String correlationId;
    private String eventType;
    private String schema;
    private String tableName;
    private String createdBy;
    private LocalDateTime timestamp;
    private List<String> metadata;
    private String details;
}