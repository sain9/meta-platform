package com.hussain.workflow_orchestrator.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkflowConfig(
        UUID id,
        @NotBlank(message = "workflow_name must not be blank")
        String workflowName,
        String description,
        JsonNode dataQuery,
        @NotNull(message = "source_sink_config must not be null")
        JsonNode sourceSinkConfig,
        @NotNull(message = "execution_config must not be null")
        JsonNode executionConfig,
        boolean enabled,
        @NotBlank(message = "created_by must not be blank")
        String createdBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {}