package com.hussain.workflow_orchestrator.schema;

import com.hussain.data_api_client.DataApiClient;
import com.hussain.data_api_client.model.DataApiRequest;
import com.hussain.data_api_client.model.enums.Operation;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Detects at startup whether workflow_config / workflow_audit exist.
 * If not, logs the finalized Definition API payloads (pretty JSON) so an operator
 * can create them manually via:
 *
 *   curl -X POST "http://localhost:9996/api/v1/schema/create-and-register" \
 *        -H "Content-Type: application/json" -d '<payload>'
 *
 * This class never creates tables itself and never aborts startup.
 */
@Component
public class SchemaBootstrapLogger {

    private static final Logger log = LoggerFactory.getLogger(SchemaBootstrapLogger.class);

    private final DataApiClient dataApiClient;

    public SchemaBootstrapLogger(DataApiClient dataApiClient) {
        this.dataApiClient = dataApiClient;
    }

    @PostConstruct
    void checkTables() {
        boolean configExists = tableExists("workflow_config");
        if (!configExists) {
            log.warn("""
                    ============================================================
                    Table 'workflow_config' is missing. Create it manually using:
                      POST http://localhost:9996/api/v1/schema/create-and-register
                      Content-Type: application/json
                    Payload:
                    {}
                    ============================================================""",
                    WORKFLOW_CONFIG_DDL);
        } else {
            log.info("Schema check OK: workflow_config present.");
        }

        boolean auditExists = tableExists("workflow_audit");
        if (!auditExists) {
            log.warn("""
                    ============================================================
                    Table 'workflow_audit' is missing. Create it manually using:
                      POST http://localhost:9996/api/v1/schema/create-and-register
                      Content-Type: application/json
                    Payload:
                    {}
                    ============================================================""",
                    WORKFLOW_AUDIT_DDL);
        } else {
            log.info("Schema check OK: workflow_audit present.");
        }
    }

    private boolean tableExists(String tableName) {
        try {
            dataApiClient.read(DataApiRequest.builder()
                    .operation(Operation.READ)
                    .entity(tableName)
                    .column("*")
                    .limit(1)
                    .build());
            return true;
        } catch (RuntimeException ex) {
            log.debug("Table '{}' probe failed: {}", tableName, ex.getMessage());
            return false;
        }
    }

    // ---------- Finalized DDL payloads (kept in sync with approved schema) ----------

    private static final String WORKFLOW_CONFIG_DDL = """
            {
              "schema": "public",
              "tableName": "workflow_config",
              "ifNotExists": true,
              "tableComment": "Stores workflow definitions, source and sink configurations, Data API queries, and execution settings.",
              "columns": [
                { "name": "id", "type": "UUID", "nullable": false, "defaultValue": "GEN_RANDOM_UUID()" },
                { "name": "workflow_name", "type": "VARCHAR", "length": 255, "nullable": false },
                { "name": "data_query", "type": "JSONB", "nullable": true },
                { "name": "source_sink_config", "type": "JSONB", "nullable": false },
                { "name": "execution_config", "type": "JSONB", "nullable": false },
                { "name": "enabled", "type": "BOOLEAN", "nullable": false, "defaultValue": "TRUE" },
                { "name": "created_by", "type": "VARCHAR", "length": 255, "nullable": false },
                { "name": "created_at", "type": "TIMESTAMPTZ", "nullable": false, "defaultValue": "CURRENT_TIMESTAMP" },
                { "name": "updated_at", "type": "TIMESTAMPTZ", "nullable": false, "defaultValue": "CURRENT_TIMESTAMP" },
                { "name": "description", "type": "TEXT", "nullable": true }
              ],
              "constraints": {
                "primaryKeys": [
                  { "name": "pk_workflow_config", "columns": ["id"] }
                ],
                "uniqueConstraints": [
                  { "name": "uk_workflow_config_name", "columns": ["workflow_name"] }
                ]
              }
            }
            """;

    private static final String WORKFLOW_AUDIT_DDL = """
            {
              "schema": "public",
              "tableName": "workflow_audit",
              "ifNotExists": true,
              "tableComment": "Stores workflow execution history, execution metrics, runtime details, and errors.",
              "columns": [
                { "name": "id", "type": "UUID", "nullable": false, "defaultValue": "GEN_RANDOM_UUID()" },
                { "name": "workflow_id", "type": "UUID", "nullable": false },
                { "name": "status", "type": "VARCHAR", "length": 20, "nullable": false },
                { "name": "trigger_type", "type": "VARCHAR", "length": 20, "nullable": false },
                { "name": "started_at", "type": "TIMESTAMPTZ", "nullable": true },
                { "name": "completed_at", "type": "TIMESTAMPTZ", "nullable": true },
                { "name": "execution_metrics", "type": "JSONB", "nullable": true },
                { "name": "execution_details", "type": "JSONB", "nullable": true },
                { "name": "error_message", "type": "TEXT", "nullable": true },
                { "name": "created_at", "type": "TIMESTAMPTZ", "nullable": false, "defaultValue": "CURRENT_TIMESTAMP" }
              ],
              "constraints": {
                "primaryKeys": [
                  { "name": "pk_workflow_audit", "columns": ["id"] }
                ],
                "foreignKeys": [
                  {
                    "name": "fk_workflow_audit_config",
                    "columns": ["workflow_id"],
                    "referencedTable": "workflow_config",
                    "referencedColumns": ["id"],
                    "onDelete": "RESTRICT",
                    "onUpdate": "CASCADE"
                  }
                ]
              }
            }
            """;
}