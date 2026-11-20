package com.hussain.workflow_orchestrator.runtime.scheduler;

import com.hussain.workflow_orchestrator.domain.CachedWorkflow;
import com.hussain.workflow_orchestrator.domain.ExecutionConfig;
import com.hussain.workflow_orchestrator.domain.enums.AuditStatus;
import com.hussain.workflow_orchestrator.domain.enums.TriggerType;
import com.hussain.workflow_orchestrator.metrics.WorkflowMetrics;
import com.hussain.workflow_orchestrator.repository.WorkflowAuditRepository;
import com.hussain.workflow_orchestrator.service.AsyncExecutionEngine;
import com.hussain.workflow_orchestrator.service.QueueFullException;
import jakarta.annotation.PreDestroy;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

@Component
public class WorkflowSchedulerArmer {

    private static final Logger log = LoggerFactory.getLogger(WorkflowSchedulerArmer.class);

    private final ThreadPoolTaskScheduler scheduler;
    private final ArmedWorkflowRegistry registry;
    private final WorkflowAuditRepository auditRepo;
    private final AsyncExecutionEngine engine;
    private final WorkflowMetrics metrics;

    public WorkflowSchedulerArmer(
            ArmedWorkflowRegistry registry,
            WorkflowAuditRepository auditRepo,
            AsyncExecutionEngine engine,
            WorkflowMetrics metrics) {
        this.registry = registry;
        this.auditRepo = auditRepo;
        this.engine = engine;
        this.metrics = metrics;

        this.scheduler = new ThreadPoolTaskScheduler();
        this.scheduler.setPoolSize(4);
        this.scheduler.setThreadNamePrefix("wf-scheduler-");
        this.scheduler.setWaitForTasksToCompleteOnShutdown(true);
        this.scheduler.setAwaitTerminationSeconds(10);
        this.scheduler.initialize();
    }

    public void reconcile(Collection<CachedWorkflow> allWorkflows) {
        Set<String> currentlyArmed = registry.armedNames();
        Set<String> shouldBeArmed = new HashSet<>();
        for (CachedWorkflow wf : allWorkflows) {
            if (wf.isScheduled()) {
                shouldBeArmed.add(wf.name());
            }
        }

        Set<String> toDisarm = new HashSet<>(currentlyArmed);
        toDisarm.removeAll(shouldBeArmed);
        Set<String> toArm = new HashSet<>(shouldBeArmed);
        toArm.removeAll(currentlyArmed);

        log.debug("[scheduler] reconcile — armed={}, shouldBeArmed={}, toArm={}, toDisarm={}",
                currentlyArmed, shouldBeArmed, toArm, toDisarm);

        for (String name : toDisarm) {
            registry.remove(name);
        }

        for (CachedWorkflow wf : allWorkflows) {
            if (wf.isScheduled() && (toArm.contains(wf.name()) || !registry.isArmed(wf.name()))) {
                arm(wf);
            }
        }

        log.info("[scheduler] reconcile summary — armed={}, toArm={}, disarmed={}",
                registry.armedNames().size(), toArm.size(), toDisarm.size());
    }

    private void arm(CachedWorkflow wf) {
        ExecutionConfig cfg = wf.executionConfig();
        ScheduledFuture<?> future;

        try {
            if (cfg.cron() != null && !cfg.cron().isBlank()) {
                ZoneId zone = ZoneId.of(cfg.zone());
                future = scheduler.schedule(() -> fire(wf), new CronTrigger(cfg.cron(), zone));
                log.info("[scheduler][ARMED] '{}' cron='{}' zone='{}'", wf.name(), cfg.cron(), cfg.zone());
            } else if (cfg.fixedDelayMillis() != null) {
                future = scheduler.scheduleWithFixedDelay(() -> fire(wf), cfg.fixedDelayMillis());
                log.info("[scheduler][ARMED] '{}' fixedDelay={}ms", wf.name(), cfg.fixedDelayMillis());
            } else {
                log.warn("[scheduler] workflow '{}' is SCHEDULED but has no cron/fixedDelayMillis — skipped",
                        wf.name());
                return;
            }
        } catch (IllegalArgumentException ex) {
            log.error("[scheduler] FAILED to arm workflow '{}': {}", wf.name(), ex.getMessage());
            return;
        }

        registry.put(wf.name(), future);
    }

    private void fire(CachedWorkflow wf) {
        try {
            UUID auditId = UUID.randomUUID();
            OffsetDateTime now = OffsetDateTime.now();

            Map<String, Object> auditRow = new HashMap<>();
            auditRow.put("id", auditId.toString());
            auditRow.put("workflow_id", wf.id().toString());
            auditRow.put("status", AuditStatus.SUCCESS.name());
            auditRow.put("trigger_type", TriggerType.SCHEDULED.name());
            auditRow.put("started_at", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            auditRow.put("created_at", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            auditRepo.create(auditRow);

            log.info("[scheduler][FIRE] workflow '{}' (auditId={})", wf.name(), auditId);
            metrics.recordScheduledFire();
            engine.submit(wf, auditId, TriggerType.SCHEDULED);
        } catch (QueueFullException qfe) {
            log.warn("[scheduler][SKIPPED] workflow '{}' — engine queue full; will retry next tick",
                    wf.name());
        } catch (Exception ex) {
            log.error("[scheduler] FIRE FAILED for '{}': {}", wf.name(), ex.getMessage(), ex);
        }
    }

    @PreDestroy
    void shutdown() {
        registry.cancelAll();
        scheduler.shutdown();
        log.info("[scheduler][SHUTDOWN] WorkflowSchedulerArmer complete");
    }
}