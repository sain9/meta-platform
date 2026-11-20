package com.hussain.workflow_orchestrator.web.dto;

import com.hussain.workflow_orchestrator.domain.enums.AuditStatus;
import com.hussain.workflow_orchestrator.domain.enums.TriggerType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TriggerResponse(
        UUID auditId,
        UUID workflowId,
        String workflowName,
        AuditStatus status,
        TriggerType triggerType,
        OffsetDateTime startedAt) {}