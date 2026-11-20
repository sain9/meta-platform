package com.hussain.workflow_orchestrator.metrics;

import com.hussain.workflow_orchestrator.domain.enums.AuditStatus;
import com.hussain.workflow_orchestrator.domain.enums.TriggerType;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import org.springframework.stereotype.Component;

/**
 * Central facade for all Micrometer counters/timers.
 *
 * Metrics emitted:
 *   workflow.triggered.total{trigger}       counter — 1 per API/MANUAL trigger
 *   workflow.scheduled.fire.total           counter — 1 per scheduled tick
 *   workflow.runs.total{trigger,result}     counter — 1 per terminal run outcome
 *   workflow.run.duration                   timer   — execution duration
 *   workflow.poll.duration                  timer   — refreshWorkflowCache duration
 *   workflow.polls.total{result}            counter — success/failure per poll
 */
@Component
public class WorkflowMetrics {

    private final MeterRegistry registry;

    public WorkflowMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordTrigger(TriggerType trigger) {
        registry.counter("workflow.triggered.total",
                "trigger", trigger.name()).increment();
    }

    public void recordScheduledFire() {
        registry.counter("workflow.scheduled.fire.total").increment();
    }

    public void recordRun(TriggerType trigger, AuditStatus result) {
        registry.counter("workflow.runs.total",
                "trigger", trigger.name(),
                "result", result.name()).increment();
    }

    public void recordRunDuration(TriggerType trigger, AuditStatus result, long millis) {
        Timer.builder("workflow.run.duration")
                .tag("trigger", trigger.name())
                .tag("result", result.name())
                .register(registry)
                .record(Duration.ofMillis(millis));
    }

    public void recordPoll(boolean success, long millis) {
        registry.counter("workflow.polls.total",
                "result", success ? "SUCCESS" : "FAILED").increment();
        Timer.builder("workflow.poll.duration")
                .tag("result", success ? "SUCCESS" : "FAILED")
                .register(registry)
                .record(Duration.ofMillis(millis));
    }
}