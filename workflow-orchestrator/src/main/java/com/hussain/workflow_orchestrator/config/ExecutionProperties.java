package com.hussain.workflow_orchestrator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds:
 *   workflow.orchestrator.execution.timeout
 *   workflow.orchestrator.max-concurrent-workflows
 *   workflow.orchestrator.queue-capacity   (new)
 */
@ConfigurationProperties(prefix = "workflow.orchestrator")
public record ExecutionProperties(
        Execution execution,
        Integer maxConcurrentWorkflows,
        Integer queueCapacity) {

    public record Execution(Long timeout) {}

    public ExecutionProperties {
        if (maxConcurrentWorkflows == null || maxConcurrentWorkflows < 1) {
            maxConcurrentWorkflows = 10;
        }
        if (queueCapacity == null || queueCapacity < 1) {
            queueCapacity = 100;
        }
        if (execution == null) {
            execution = new Execution(300_000L);
        } else if (execution.timeout() == null || execution.timeout() < 1000L) {
            execution = new Execution(300_000L);
        }
    }

    public long globalTimeoutMillis() {
        return execution.timeout();
    }
}