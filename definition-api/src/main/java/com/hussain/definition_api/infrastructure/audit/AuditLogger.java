package com.hussain.definition_api.infrastructure.audit;

import com.hussain.definition_api.core.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuditLogger {

    public void logTableCreation(SchemaDefinition schemaDefinition) {
        log.info("AUDIT: Table created - Schema: {}, Table: {}, Columns: {}",
                schemaDefinition.getSchema(),
                schemaDefinition.getTableName(),
                schemaDefinition.getColumns().size());
    }

    public void logTableDrop(String schema, String tableName) {
        log.info("AUDIT: Table dropped - Schema: {}, Table: {}", schema, tableName);
    }

    public void logColumnAddition(String schema, String tableName, ColumnDefinition column) {
        log.info("AUDIT: Column added - Schema: {}, Table: {}, Column: {}, Type: {}",
                schema, tableName, column.getName(), column.getType());
    }

    public void logColumnDrop(String schema, String tableName, String columnName) {
        log.info("AUDIT: Column dropped - Schema: {}, Table: {}, Column: {}",
                schema, tableName, columnName);
    }

    // Fix: Change parameter type to match the Constraint class
    public void logConstraintAddition(String schema, String tableName, com.hussain.definition_api.core.model.Constraint constraint) {
        log.info("AUDIT: Constraint added - Schema: {}, Table: {}, Constraint: {}, Type: {}",
                schema, tableName, constraint.getName(), constraint.getType());
    }

    public void logConstraintDrop(String schema, String tableName, String constraintName) {
        log.info("AUDIT: Constraint dropped - Schema: {}, Table: {}, Constraint: {}",
                schema, tableName, constraintName);
    }

    public void logSchemaChange(SchemaAuditEvent event) {
        log.info("AUDIT: Schema change - CorrelationId: {}, Event: {}, Schema: {}, Table: {}, User: {}",
                event.getCorrelationId(),
                event.getEventType(),
                event.getSchema(),
                event.getTableName(),
                event.getCreatedBy());
    }
}