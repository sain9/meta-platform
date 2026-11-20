package com.hussain.workflow_orchestrator.config;

import com.hussain.workflow_orchestrator.runtime.scheduler.ArmedWorkflowRegistry;
import com.hussain.workflow_orchestrator.runtime.store.WorkflowStore;
import com.hussain.workflow_orchestrator.service.DefaultAsyncExecutionEngine;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Configuration;

/**
 * Registers Micrometer gauges backed by live bean state.
 *   workflow.cache.size             -> WorkflowStore.size()
 *   workflow.scheduler.armed.count  -> ArmedWorkflowRegistry.armedNames().size()
 *   workflow.engine.active          -> ThreadPoolExecutor.getActiveCount()
 *   workflow.engine.queue.size      -> ThreadPoolExecutor.getQueue().size()
 */
@Configuration
public class MetricsConfig {

    public MetricsConfig(
            MeterRegistry registry,
            WorkflowStore workflowStore,
            ArmedWorkflowRegistry armedRegistry,
            DefaultAsyncExecutionEngine engine) {

        registry.gauge("workflow.cache.size", workflowStore, WorkflowStore::size);
        registry.gauge("workflow.scheduler.armed.count", armedRegistry,
                r -> r.armedNames().size());
        registry.gauge("workflow.engine.active", engine, DefaultAsyncExecutionEngine::activeCount);
        registry.gauge("workflow.engine.queue.size", engine,
                DefaultAsyncExecutionEngine::queueSize);
    }
}