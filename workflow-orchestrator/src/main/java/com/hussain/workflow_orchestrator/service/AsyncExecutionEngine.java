package com.hussain.workflow_orchestrator.service;

import com.hussain.workflow_orchestrator.domain.CachedWorkflow;
import com.hussain.workflow_orchestrator.domain.enums.TriggerType;
import java.util.UUID;

/**
 * Interface for async workflow execution.
 * Stage 4 ships a stub; Stage 6 fills real logic (thread pool + timeout + audit writes).
 */
public interface AsyncExecutionEngine {

    /**
     * Fire-and-forget execution.
     *
     * @param workflow     cached workflow config
     * @param auditId      pre-created audit row id (status=PENDING)
     * @param triggerType  provenance — API, SCHEDULED, MANUAL, EVENT, RETRY
     */
    void submit(CachedWorkflow workflow, UUID auditId, TriggerType triggerType);
}