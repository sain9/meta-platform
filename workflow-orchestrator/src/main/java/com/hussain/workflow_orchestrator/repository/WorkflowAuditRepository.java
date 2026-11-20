package com.hussain.workflow_orchestrator.repository;

import static com.hussain.workflow_orchestrator.constant.WorkflowAuditColumns.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.hussain.data_api_client.DataApiClient;
import com.hussain.data_api_client.model.DataApiRequest;
import com.hussain.data_api_client.model.Row;
import com.hussain.data_api_client.model.enums.FilterOp;
import com.hussain.data_api_client.model.enums.Operation;
import com.hussain.workflow_orchestrator.domain.WorkflowAudit;
import com.hussain.workflow_orchestrator.domain.enums.AuditStatus;
import com.hussain.workflow_orchestrator.domain.enums.TriggerType;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * gRPC CRUD wrapper for workflow_audit table.
 */
@Service
public class WorkflowAuditRepository {

    private static final Logger log = LoggerFactory.getLogger(WorkflowAuditRepository.class);

    private final DataApiClient dataApiClient;
    private final RowMapperHelper mapper;

    public WorkflowAuditRepository(DataApiClient dataApiClient, RowMapperHelper mapper) {
        this.dataApiClient = dataApiClient;
        this.mapper = mapper;
    }

    public int create(Map<String, Object> data) {
        log.debug("create() entity={} keys={}", TABLE, data.keySet());
        return dataApiClient.create(TABLE, data);
    }

    public List<WorkflowAudit> findByWorkflowId(UUID workflowId) {
        DataApiRequest request = DataApiRequest.builder()
                .operation(Operation.READ)
                .entity(TABLE)
                .column(ID)
                .column(WORKFLOW_ID)
                .column(STATUS)
                .column(TRIGGER_TYPE)
                .column(STARTED_AT)
                .column(COMPLETED_AT)
                .column(EXECUTION_METRICS)
                .column(EXECUTION_DETAILS)
                .column(ERROR_MESSAGE)
                .column(CREATED_AT)
                .filter(DataApiRequest.Filter.builder()
                        .column(WORKFLOW_ID)
                        .operator(FilterOp.EQ)
                        .value(workflowId.toString())
                        .build())
                .build();

        return dataApiClient.read(request).stream().map(this::toDomain).toList();
    }

    public WorkflowAudit findById(UUID id) {
        DataApiRequest request = DataApiRequest.builder()
                .operation(Operation.READ)
                .entity(TABLE)
                .column(ID)
                .column(WORKFLOW_ID)
                .column(STATUS)
                .column(TRIGGER_TYPE)
                .column(STARTED_AT)
                .column(COMPLETED_AT)
                .column(EXECUTION_METRICS)
                .column(EXECUTION_DETAILS)
                .column(ERROR_MESSAGE)
                .column(CREATED_AT)
                .filter(DataApiRequest.Filter.builder()
                        .column(ID)
                        .operator(FilterOp.EQ)
                        .value(id.toString())
                        .build())
                .limit(1)
                .build();

        List<Row> rows = dataApiClient.read(request);
        return rows.isEmpty() ? null : toDomain(rows.get(0));
    }

    public int updateStatus(
            UUID auditId,
            AuditStatus status,
            String errorMessage,
            OffsetDateTime startedAt,
            OffsetDateTime completedAt,
            JsonNode executionMetrics,
            JsonNode executionDetails) {

        Map<String, Object> data = new HashMap<>();
        data.put(STATUS, status.name());
        if (errorMessage != null) {
            data.put(ERROR_MESSAGE, errorMessage);
        }
        if (startedAt != null) {
            data.put(STARTED_AT, startedAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }
        if (completedAt != null) {
            data.put(COMPLETED_AT, completedAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }
        if (executionMetrics != null) {
            data.put(EXECUTION_METRICS, executionMetrics.toString());
        }
        if (executionDetails != null) {
            data.put(EXECUTION_DETAILS, executionDetails.toString());
        }

        log.debug("updateStatus() id={} status={}", auditId, status);
        return dataApiClient.update(TABLE, data,
                DataApiRequest.Filter.builder()
                        .column(ID)
                        .operator(FilterOp.EQ)
                        .value(auditId.toString())
                        .build());
    }

    private WorkflowAudit toDomain(Row row) {
        return new WorkflowAudit(
                mapper.asUuid(row, ID),
                mapper.asUuid(row, WORKFLOW_ID),
                parseAuditStatus(mapper.asString(row, STATUS)),
                parseTriggerType(mapper.asString(row, TRIGGER_TYPE)),
                mapper.asOffsetDateTime(row, STARTED_AT),
                mapper.asOffsetDateTime(row, COMPLETED_AT),
                mapper.asJsonNode(row, EXECUTION_METRICS),
                mapper.asJsonNode(row, EXECUTION_DETAILS),
                mapper.asString(row, ERROR_MESSAGE),
                mapper.asOffsetDateTime(row, CREATED_AT));
    }

    private AuditStatus parseAuditStatus(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return AuditStatus.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown AuditStatus '{}' in DB row — mapped to null", raw);
            return null;
        }
    }

    private TriggerType parseTriggerType(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return TriggerType.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown TriggerType '{}' in DB row — mapped to null", raw);
            return null;
        }
    }
}