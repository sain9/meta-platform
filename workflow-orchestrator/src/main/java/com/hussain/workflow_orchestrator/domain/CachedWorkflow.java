package com.hussain.workflow_orchestrator.domain;

import com.hussain.workflow_orchestrator.domain.enums.ExecutionType;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Runtime cache entry. Wraps WorkflowConfig with the parse of execution_config
 * (so we don't re-parse JSONB on every trigger) and a loadedAt timestamp.
 */
public record CachedWorkflow(
        UUID id,
        String name,
        WorkflowConfig config,
        ExecutionConfig executionConfig,
        OffsetDateTime loadedAt) {

    public ExecutionType executionType() {
        return executionConfig.type();
    }

    public boolean isScheduled() {
        return executionConfig.isScheduled();
    }
}