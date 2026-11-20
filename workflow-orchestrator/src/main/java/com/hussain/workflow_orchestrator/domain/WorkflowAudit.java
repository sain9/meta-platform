package com.hussain.workflow_orchestrator.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.hussain.workflow_orchestrator.domain.enums.AuditStatus;
import com.hussain.workflow_orchestrator.domain.enums.TriggerType;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkflowAudit(
        UUID id,
        @NotNull(message = "workflow_id must not be null")
        UUID workflowId,
        @NotNull(message = "status must not be null")
        AuditStatus status,
        @NotNull(message = "trigger_type must not be null")
        TriggerType triggerType,
        OffsetDateTime startedAt,
        OffsetDateTime completedAt,
        JsonNode executionMetrics,
        JsonNode executionDetails,
        String errorMessage,
        OffsetDateTime createdAt) {}