package com.hussain.definition_api.api.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Jacksonized
public class SchemaCreationResponse {
    private boolean success;
    private String correlationId;
    private String schema;
    private String tableName;
    private List<String> ddlStatements;
    private LocalDateTime createdAt;
    private String message;
}