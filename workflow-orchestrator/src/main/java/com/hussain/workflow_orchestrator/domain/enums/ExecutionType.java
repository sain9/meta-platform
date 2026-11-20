package com.hussain.workflow_orchestrator.domain.enums;

/**
 * Declares how a workflow is designed to be triggered.
 *
 * <p>All enabled workflows are triggerable via POST /workflow/{name} regardless of type —
 * this enum only controls whether the orchestrator ADDITIONALLY auto-arms the workflow at boot.
 */
public enum ExecutionType {

    /** On-demand only. No auto-schedule. Triggerable via POST /workflow/{name}. */
    MANUAL,

    /** Auto-armed with cron or fixedDelayMillis at startup. Also triggerable via API. */
    SCHEDULED,

    /** Reserved for future event-driven execution. Currently only triggerable via API. */
    EVENT
}