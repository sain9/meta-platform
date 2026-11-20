package com.hussain.workflow_orchestrator.service;

import com.hussain.workflow_orchestrator.domain.CachedWorkflow;
import java.util.UUID;

/**
 * The actual "what does executing a workflow mean" seam.
 * Stage 6 ships NoOpWorkflowExecutor; replace with real logic when ready.
 *
 * Throwing from execute() -> audit marks FAILED with the exception message.
 */
public interface WorkflowExecutor {
    void execute(CachedWorkflow workflow, UUID auditId) throws Exception;
}