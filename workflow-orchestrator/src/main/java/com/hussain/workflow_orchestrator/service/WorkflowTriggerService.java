package com.hussain.workflow_orchestrator.service;

import com.hussain.workflow_orchestrator.constant.WorkflowAuditColumns;
import com.hussain.workflow_orchestrator.domain.CachedWorkflow;
import com.hussain.workflow_orchestrator.domain.enums.AuditStatus;
import com.hussain.workflow_orchestrator.domain.enums.TriggerType;
import com.hussain.workflow_orchestrator.metrics.WorkflowMetrics;
import com.hussain.workflow_orchestrator.repository.WorkflowAuditRepository;
import com.hussain.workflow_orchestrator.web.dto.TriggerResponse;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WorkflowTriggerService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowTriggerService.class);

    private final WorkflowService workflowService;
    private final WorkflowAuditRepository auditRepo;
    private final AsyncExecutionEngine engine;
    private final WorkflowMetrics metrics;

    public WorkflowTriggerService(
            WorkflowService workflowService,
            WorkflowAuditRepository auditRepo,
            AsyncExecutionEngine engine,
            WorkflowMetrics metrics) {
        this.workflowService = workflowService;
        this.auditRepo = auditRepo;
        this.engine = engine;
        this.metrics = metrics;
    }

    public TriggerResponse triggerByName(String name) {
        CachedWorkflow cached = workflowService.findCached(name);
        if (cached == null) {
            if (workflowService.ensureWorkflowLoaded(name)) {
                cached = workflowService.findCached(name);
            }
        }
        if (cached == null || !cached.config().enabled()) {
            throw new WorkflowNotFoundException(name);
        }

        UUID auditId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        Map<String, Object> auditRow = new HashMap<>();
        auditRow.put(WorkflowAuditColumns.ID, auditId.toString());
        auditRow.put(WorkflowAuditColumns.WORKFLOW_ID, cached.id().toString());
        auditRow.put(WorkflowAuditColumns.STATUS, AuditStatus.SUCCESS.name());
        auditRow.put(WorkflowAuditColumns.TRIGGER_TYPE, TriggerType.API.name());
        auditRow.put(WorkflowAuditColumns.STARTED_AT, now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        auditRow.put(WorkflowAuditColumns.CREATED_AT, now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        auditRepo.create(auditRow);

        metrics.recordTrigger(TriggerType.API);
        engine.submit(cached, auditId, TriggerType.API);

        log.info("Triggered workflow '{}' (auditId={})", name, auditId);
        return new TriggerResponse(
                auditId, cached.id(), cached.name(),
                AuditStatus.SUCCESS, TriggerType.API, now);
    }

    public static class WorkflowNotFoundException extends RuntimeException {
        public WorkflowNotFoundException(String name) {
            super("workflow not found: " + name);
        }
    }
}