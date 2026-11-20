package com.hussain.workflow_orchestrator.repository;

import static com.hussain.workflow_orchestrator.constant.WorkflowConfigColumns.*;

import com.hussain.data_api_client.DataApiClient;
import com.hussain.data_api_client.model.DataApiRequest;
import com.hussain.data_api_client.model.Row;
import com.hussain.data_api_client.model.enums.FilterOp;
import com.hussain.data_api_client.model.enums.Operation;
import com.hussain.workflow_orchestrator.domain.WorkflowConfig;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * gRPC CRUD wrapper for workflow_config table.
 */
@Service
public class WorkflowConfigRepository {

    private static final Logger log = LoggerFactory.getLogger(WorkflowConfigRepository.class);

    private final DataApiClient dataApiClient;
    private final RowMapperHelper mapper;

    public WorkflowConfigRepository(DataApiClient dataApiClient, RowMapperHelper mapper) {
        this.dataApiClient = dataApiClient;
        this.mapper = mapper;
    }

    public List<WorkflowConfig> findAllEnabled() {
        DataApiRequest request = DataApiRequest.builder()
                .operation(Operation.READ)
                .entity(TABLE)
                .column(ID)
                .column(WORKFLOW_NAME)
                .column(DESCRIPTION)
                .column(DATA_QUERY)
                .column(SOURCE_SINK_CONFIG)
                .column(EXECUTION_CONFIG)
                .column(ENABLED)
                .column(CREATED_BY)
                .column(CREATED_AT)
                .column(UPDATED_AT)
                .filter(DataApiRequest.Filter.builder()
                        .column(ENABLED)
                        .operator(FilterOp.EQ)
                        .value(true)
                        .build())
                .build();

        List<Row> rows = dataApiClient.read(request);
        log.debug("findAllEnabled() -> {} row(s)", rows.size());
        return rows.stream().map(this::toDomain).toList();
    }

    public WorkflowConfig findByName(String workflowName) {
        DataApiRequest request = DataApiRequest.builder()
                .operation(Operation.READ)
                .entity(TABLE)
                .column(ID)
                .column(WORKFLOW_NAME)
                .column(DESCRIPTION)
                .column(DATA_QUERY)
                .column(SOURCE_SINK_CONFIG)
                .column(EXECUTION_CONFIG)
                .column(ENABLED)
                .column(CREATED_BY)
                .column(CREATED_AT)
                .column(UPDATED_AT)
                .filter(DataApiRequest.Filter.builder()
                        .column(WORKFLOW_NAME)
                        .operator(FilterOp.EQ)
                        .value(workflowName)
                        .build())
                .limit(1)
                .build();

        List<Row> rows = dataApiClient.read(request);
        if (rows.isEmpty()) {
            return null;
        }
        return toDomain(rows.get(0));
    }

    public int create(Map<String, Object> data) {
        log.debug("create() entity={} keys={}", TABLE, data.keySet());
        return dataApiClient.create(TABLE, data);
    }

    public int update(UUID id, Map<String, Object> data) {
        log.debug("update() entity={} id={} keys={}", TABLE, id, data.keySet());
        return dataApiClient.update(TABLE, data,
                DataApiRequest.Filter.builder()
                        .column(ID)
                        .operator(FilterOp.EQ)
                        .value(id.toString())
                        .build());
    }

    public int delete(UUID id) {
        log.debug("delete() entity={} id={}", TABLE, id);
        return dataApiClient.delete(TABLE,
                DataApiRequest.Filter.builder()
                        .column(ID)
                        .operator(FilterOp.EQ)
                        .value(id.toString())
                        .build());
    }

    private WorkflowConfig toDomain(Row row) {
        Boolean enabled = mapper.asBoolean(row, ENABLED);
        return new WorkflowConfig(
                mapper.asUuid(row, ID),
                mapper.asString(row, WORKFLOW_NAME),
                mapper.asString(row, DESCRIPTION),
                mapper.asJsonNode(row, DATA_QUERY),
                mapper.asJsonNode(row, SOURCE_SINK_CONFIG),
                mapper.asJsonNode(row, EXECUTION_CONFIG),
                enabled != null && enabled,
                mapper.asString(row, CREATED_BY),
                mapper.asOffsetDateTime(row, CREATED_AT),
                mapper.asOffsetDateTime(row, UPDATED_AT));
    }
}