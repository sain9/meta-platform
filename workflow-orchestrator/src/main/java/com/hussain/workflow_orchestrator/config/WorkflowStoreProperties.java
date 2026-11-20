package com.hussain.workflow_orchestrator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "workflow.store")
public record WorkflowStoreProperties(boolean enabled, long pollIntervalMs) {

    public WorkflowStoreProperties {
        if (pollIntervalMs <= 0) {
            pollIntervalMs = 60_000L;
        }
    }
}