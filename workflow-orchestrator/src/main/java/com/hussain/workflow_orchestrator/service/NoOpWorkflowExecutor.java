package com.hussain.workflow_orchestrator.service;

import com.hussain.workflow_orchestrator.domain.CachedWorkflow;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Stage 6 placeholder. Real execution logic lands here later.
 * Just logs and returns -> engine marks audit SUCCESS.
 */
@Service
public class NoOpWorkflowExecutor implements WorkflowExecutor {

    private static final Logger log = LoggerFactory.getLogger(NoOpWorkflowExecutor.class);

    @Override
    public void execute(CachedWorkflow workflow, UUID auditId) {
        log.info("[executor][NOOP] workflow '{}' (auditId={}) — no-op complete",
                workflow.name(), auditId);
    }
}