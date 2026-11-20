package com.hussain.workflow_orchestrator.domain;

import com.hussain.workflow_orchestrator.domain.enums.ExecutionType;

/**
 * Parsed representation of workflow_config.execution_config (JSONB).
 *
 * <p>Schema:
 * <pre>
 * {
 *   "type": "MANUAL" | "SCHEDULED" | "EVENT",   // default MANUAL
 *   "cron": "0 *&#47;5 * * * *",                    // only when type=SCHEDULED
 *   "fixedDelayMillis": 300000,                 // alternative to cron, >= 1000
 *   "zone": "UTC",                              // default UTC
 *   "timeoutMillis": 300000                     // default from global config
 * }
 * </pre>
 *
 * <p>All fields nullable except effective defaults resolved at parse time.
 */
public record ExecutionConfig(
        ExecutionType type,
        String cron,
        Long fixedDelayMillis,
        String zone,
        Long timeoutMillis) {

    public boolean isScheduled() {
        return type == ExecutionType.SCHEDULED;
    }
}