package com.hussain.definition_api.core.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Jacksonized
public class EntityStore {
    private UUID id;
    private String schemaName;
    private String tableName;
    private String entityDefinition;
    private boolean isEnabled;
    private String version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}