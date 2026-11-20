package com.hussain.workflow_orchestrator.health;

import com.hussain.workflow_orchestrator.runtime.scheduler.ArmedWorkflowRegistry;
import com.hussain.workflow_orchestrator.runtime.store.WorkflowStore;
import com.hussain.workflow_orchestrator.service.DefaultAsyncExecutionEngine;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Exposes /actuator/health -> workflowOrchestrator.
 *
 * DOWN after 3 consecutive poll failures (transient failures tolerated).
 */
@Component("workflowOrchestrator")
public class WorkflowOrchestratorHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(WorkflowOrchestratorHealthIndicator.class);

    private final WorkflowStore workflowStore;
    private final ArmedWorkflowRegistry armedRegistry;
    private final DefaultAsyncExecutionEngine engine;

    // Updated by WorkflowService on every poll
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicReference<OffsetDateTime> lastPollAt = new AtomicReference<>(null);
    private final AtomicReference<String> lastPollError = new AtomicReference<>(null);

    public WorkflowOrchestratorHealthIndicator(
            WorkflowStore workflowStore,
            ArmedWorkflowRegistry armedRegistry,
            DefaultAsyncExecutionEngine engine) {
        this.workflowStore = workflowStore;
        this.armedRegistry = armedRegistry;
        this.engine = engine;
    }

    public void markPollSuccess() {
        consecutiveFailures.set(0);
        lastPollAt.set(OffsetDateTime.now());
        lastPollError.set(null);
    }

    public void markPollFailure(String error) {
        consecutiveFailures.incrementAndGet();
        lastPollAt.set(OffsetDateTime.now());
        lastPollError.set(error);
        log.warn("[health] poll failure #{}: {}", consecutiveFailures.get(), error);
    }

    @Override
    public Health health() {
        Health.Builder builder = (consecutiveFailures.get() >= 3)
                ? Health.down()
                : Health.up();

        builder
                .withDetail("cacheSize", workflowStore.size())
                .withDetail("armedCount", armedRegistry.armedNames().size())
                .withDetail("engineActive", engine.activeCount())
                .withDetail("engineQueue", engine.queueSize())
                .withDetail("consecutiveFailures", consecutiveFailures.get())
                .withDetail("lastPollAt",
                        lastPollAt.get() != null ? lastPollAt.get().toString() : "never");

        String err = lastPollError.get();
        if (err != null) {
            builder.withDetail("lastPollError", err);
        }
        return builder.build();
    }
}