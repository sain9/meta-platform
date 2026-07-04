package com.hussain.definition_api.core.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hussain.definition_api.core.exception.ConstraintViolationException;
import com.hussain.definition_api.core.exception.SchemaManagementException;
import com.hussain.definition_api.core.exception.TableAlreadyExistsException;
import com.hussain.definition_api.core.model.*;
import com.hussain.definition_api.core.service.SchemaManagementService;
import com.hussain.definition_api.core.service.SchemaMetadataService;
import com.hussain.definition_api.core.service.SchemaValidationService;
import com.hussain.definition_api.infrastructure.audit.AuditLogger;
import com.hussain.definition_api.infrastructure.audit.SchemaAuditEvent;
import com.hussain.definition_api.infrastructure.repository.EntityStoreRepository;
import com.hussain.definition_api.infrastructure.repository.SchemaRepository;
import com.hussain.definition_api.shared.logging.PerformanceLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SchemaManagementServiceImpl implements SchemaManagementService {

    private final DSLContext dslContext;
    private final SchemaRepository schemaRepository;
    private final SchemaValidationService validationService;
    private final SchemaMetadataService metadataService;
    private final AuditLogger auditLogger;
    private final EntityStoreRepository entityStoreRepository;
    private final ObjectMapper objectMapper;

    @Override
    @PerformanceLogger
    public SchemaCreationResult createTable(SchemaDefinition schemaDefinition) {
        String correlationId = UUID.randomUUID().toString();
        log.info("[{}] Starting table creation process for {}.{}",
                correlationId, schemaDefinition.getSchema(), schemaDefinition.getTableName());

        try {
            validationService.validateSchemaDefinition(schemaDefinition);
            log.debug("[{}] Schema validation completed successfully", correlationId);

            if (schemaRepository.tableExists(schemaDefinition.getSchema(), schemaDefinition.getTableName())) {
                if (schemaDefinition.isIfNotExists()) {
                    log.info("[{}] Table already exists and ifNotExists is true, skipping creation", correlationId);
                    return SchemaCreationResult.builder()
                            .success(true)
                            .correlationId(correlationId)
                            .message("Table already exists, creation skipped")
                            .build();
                }
                throw new TableAlreadyExistsException(
                        String.format("Table %s.%s already exists",
                                schemaDefinition.getSchema(), schemaDefinition.getTableName())
                );
            }

            List<String> ddlStatements = schemaRepository.buildCreateTableDDL(schemaDefinition);
            log.debug("[{}] Generated {} DDL statements", correlationId, ddlStatements.size());

            for (String ddl : ddlStatements) {
                log.info("[{}] Executing DDL: {}", correlationId, ddl);
                dslContext.execute(ddl);
            }

            addConstraints(schemaDefinition, correlationId);

            if (schemaDefinition.getTableComment() != null && !schemaDefinition.getTableComment().isEmpty()) {
                String commentSql = schemaRepository.buildTableCommentDDL(schemaDefinition);
                log.info("[{}] Adding table comment: {}", correlationId, commentSql);
                dslContext.execute(commentSql);
            }

            auditLogger.logTableCreation(schemaDefinition);

            log.info("[{}] Table {}.{} created successfully",
                    correlationId, schemaDefinition.getSchema(), schemaDefinition.getTableName());

            return SchemaCreationResult.builder()
                    .success(true)
                    .correlationId(correlationId)
                    .tableName(schemaDefinition.getTableName())
                    .schema(schemaDefinition.getSchema())
                    .ddlStatements(ddlStatements)
                    .createdAt(LocalDateTime.now())
                    .message("Table created successfully")
                    .build();

        } catch (Exception e) {
            log.error("[{}] Error creating table: {}", correlationId, e.getMessage(), e);
            throw new SchemaManagementException(
                    "Failed to create table: " + e.getMessage(), e);
        }
    }

    @Override
    @PerformanceLogger
    public SchemaCreationResult createTableWithAudit(SchemaDefinition schemaDefinition, String createdBy) {
        String correlationId = UUID.randomUUID().toString();
        log.info("[{}] Creating table with audit for user: {}", correlationId, createdBy);

        SchemaCreationResult result = createTable(schemaDefinition);

        auditLogger.logSchemaChange(SchemaAuditEvent.builder()
                .correlationId(correlationId)
                .eventType("TABLE_CREATE")
                .schema(schemaDefinition.getSchema())
                .tableName(schemaDefinition.getTableName())
                .createdBy(createdBy)
                .timestamp(LocalDateTime.now())
                .metadata(result.getDdlStatements())
                .build());

        return result;
    }

    @Override
    @PerformanceLogger
    @Transactional
    public SchemaCreationResult createAndRegisterTable(SchemaDefinition schemaDefinition, String createdBy) {
        String correlationId = UUID.randomUUID().toString();
        log.info("[{}] Creating and registering table: {}.{}",
                correlationId, schemaDefinition.getSchema(), schemaDefinition.getTableName());

        try {
            // First create the table
            SchemaCreationResult result = createTable(schemaDefinition);

            if (result.isSuccess()) {
                try {
                    // Register in entity_store
                    String entityDefinitionJson = objectMapper.writeValueAsString(schemaDefinition);

                    // Get version from schemaDefinition if available, or use default
                    String version = "1.0";
                    // Note: version is not in SchemaDefinition, we'll use default

                    EntityStore entityStore = EntityStore.builder()
                            .id(UUID.randomUUID())
                            .schemaName(schemaDefinition.getSchema())
                            .tableName(schemaDefinition.getTableName())
                            .entityDefinition(entityDefinitionJson)
                            .isEnabled(true)
                            .version(version)
                            .createdAt(LocalDateTime.now())
                            .build();

                    entityStoreRepository.save(entityStore);

                    log.info("[{}] Table registered in entity_store: {}.{}",
                            correlationId, schemaDefinition.getSchema(), schemaDefinition.getTableName());

                    result.setMessage("Table created and registered successfully");
                } catch (Exception e) {
                    log.error("[{}] Failed to register table in entity_store: {}", correlationId, e.getMessage(), e);
                    result.setMessage("Table created but failed to register in entity_store: " + e.getMessage());
                }
            }

            return result;

        } catch (Exception e) {
            log.error("[{}] Error creating and registering table: {}", correlationId, e.getMessage(), e);
            throw new SchemaManagementException(
                    "Failed to create and register table: " + e.getMessage(), e);
        }
    }

    @Override
    @PerformanceLogger
    public void syncEnabledEntities() {
        log.info("Starting sync of enabled entities from entity_store...");

        try {
            // Get all enabled entity definitions
            List<EntityStore> enabledEntities = entityStoreRepository.findAllEnabled();
            log.info("Found {} enabled entity definitions to sync", enabledEntities.size());

            int createdCount = 0;
            int skippedCount = 0;
            int failedCount = 0;

            for (EntityStore entity : enabledEntities) {
                try {
                    String schema = entity.getSchemaName();
                    String tableName = entity.getTableName();

                    // Check if table already exists
                    if (entityStoreRepository.tableExists(schema, tableName)) {
                        log.debug("Table already exists, skipping: {}.{}", schema, tableName);
                        skippedCount++;
                        continue;
                    }

                    // Parse the entity definition
                    SchemaDefinition schemaDefinition = objectMapper.readValue(
                            entity.getEntityDefinition(),
                            SchemaDefinition.class
                    );

                    // Ensure schema name matches
                    schemaDefinition.setSchema(schema);
                    schemaDefinition.setTableName(tableName);

                    // Create the table
                    log.info("Creating table from entity_store: {}.{}", schema, tableName);
                    createTable(schemaDefinition);
                    createdCount++;

                } catch (Exception e) {
                    log.error("Failed to create table from entity_store: {}.{} - {}",
                            entity.getSchemaName(), entity.getTableName(), e.getMessage(), e);
                    failedCount++;
                }
            }

            log.info("Sync completed. Created: {}, Skipped (already existed): {}, Failed: {}",
                    createdCount, skippedCount, failedCount);

        } catch (Exception e) {
            log.error("Failed to sync enabled entities: {}", e.getMessage(), e);
            throw new SchemaManagementException("Failed to sync enabled entities: " + e.getMessage(), e);
        }
    }

    @Override
    @PerformanceLogger
    public void dropTable(String schema, String tableName) {
        String correlationId = UUID.randomUUID().toString();
        log.info("[{}] Dropping table {}.{}", correlationId, schema, tableName);

        try {
            validationService.validateIdentifiers(schema, tableName);

            if (!schemaRepository.tableExists(schema, tableName)) {
                log.warn("[{}] Table {}.{} does not exist", correlationId, schema, tableName);
                throw new SchemaManagementException("Table does not exist");
            }

            String dropSql = schemaRepository.buildDropTableDDL(schema, tableName, false);
            log.info("[{}] Executing DROP TABLE: {}", correlationId, dropSql);
            dslContext.execute(dropSql);

            // Also remove from entity_store if exists
            try {
                if (entityStoreRepository.existsBySchemaAndTable(schema, tableName)) {
                    entityStoreRepository.deleteBySchemaAndTable(schema, tableName);
                    log.info("[{}] Removed from entity_store: {}.{}", correlationId, schema, tableName);
                }
            } catch (Exception e) {
                log.warn("[{}] Failed to remove from entity_store: {}", correlationId, e.getMessage());
            }

            auditLogger.logTableDrop(schema, tableName);
            log.info("[{}] Table {}.{} dropped successfully", correlationId, schema, tableName);

        } catch (Exception e) {
            log.error("[{}] Error dropping table: {}", correlationId, e.getMessage(), e);
            throw new SchemaManagementException(
                    "Failed to drop table: " + e.getMessage(), e);
        }
    }

    @Override
    @PerformanceLogger
    public void dropTableCascade(String schema, String tableName) {
        String correlationId = UUID.randomUUID().toString();
        log.info("[{}] Dropping table {}.{} with CASCADE", correlationId, schema, tableName);

        try {
            validationService.validateIdentifiers(schema, tableName);

            String dropSql = schemaRepository.buildDropTableDDL(schema, tableName, true);
            log.info("[{}] Executing DROP TABLE CASCADE: {}", correlationId, dropSql);
            dslContext.execute(dropSql);

            // Also remove from entity_store if exists
            try {
                if (entityStoreRepository.existsBySchemaAndTable(schema, tableName)) {
                    entityStoreRepository.deleteBySchemaAndTable(schema, tableName);
                    log.info("[{}] Removed from entity_store: {}.{}", correlationId, schema, tableName);
                }
            } catch (Exception e) {
                log.warn("[{}] Failed to remove from entity_store: {}", correlationId, e.getMessage());
            }

            auditLogger.logTableDrop(schema, tableName);
            log.info("[{}] Table {}.{} dropped with CASCADE successfully", correlationId, schema, tableName);

        } catch (Exception e) {
            log.error("[{}] Error dropping table with CASCADE: {}", correlationId, e.getMessage(), e);
            throw new SchemaManagementException(
                    "Failed to drop table with CASCADE: " + e.getMessage(), e);
        }
    }

    @Override
    @PerformanceLogger
    public void truncateTable(String schema, String tableName) {
        String correlationId = UUID.randomUUID().toString();
        log.info("[{}] Truncating table {}.{}", correlationId, schema, tableName);

        try {
            validationService.validateIdentifiers(schema, tableName);

            String truncateSql = schemaRepository.buildTruncateTableDDL(schema, tableName);
            log.info("[{}] Executing TRUNCATE TABLE: {}", correlationId, truncateSql);
            dslContext.execute(truncateSql);

            log.info("[{}] Table {}.{} truncated successfully", correlationId, schema, tableName);

        } catch (Exception e) {
            log.error("[{}] Error truncating table: {}", correlationId, e.getMessage(), e);
            throw new SchemaManagementException(
                    "Failed to truncate table: " + e.getMessage(), e);
        }
    }

    @Override
    @PerformanceLogger
    public SchemaInfo getTableInfo(String schema, String tableName) {
        String correlationId = UUID.randomUUID().toString();
        log.debug("[{}] Fetching table info for {}.{}", correlationId, schema, tableName);

        try {
            validationService.validateIdentifiers(schema, tableName);
            return metadataService.getTableInfo(schema, tableName);

        } catch (Exception e) {
            log.error("[{}] Error fetching table info: {}", correlationId, e.getMessage(), e);
            throw new SchemaManagementException(
                    "Failed to fetch table info: " + e.getMessage(), e);
        }
    }

    @Override
    @PerformanceLogger
    public List<SchemaInfo> listTables(String schema) {
        String correlationId = UUID.randomUUID().toString();
        log.debug("[{}] Listing tables in schema: {}", correlationId, schema);

        try {
            validationService.validateSchemaName(schema);
            return metadataService.listTables(schema);

        } catch (Exception e) {
            log.error("[{}] Error listing tables: {}", correlationId, e.getMessage(), e);
            throw new SchemaManagementException(
                    "Failed to list tables: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean tableExists(String schema, String tableName) {
        try {
            validationService.validateIdentifiers(schema, tableName);
            return schemaRepository.tableExists(schema, tableName);
        } catch (Exception e) {
            log.error("Error checking table existence: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @PerformanceLogger
    public void addColumn(String schema, String tableName, ColumnDefinition columnDefinition) {
        String correlationId = UUID.randomUUID().toString();
        log.info("[{}] Adding column {} to table {}.{}",
                correlationId, columnDefinition.getName(), schema, tableName);

        try {
            validationService.validateIdentifiers(schema, tableName);
            validationService.validateColumnDefinition(columnDefinition);

            String alterSql = schemaRepository.buildAddColumnDDL(schema, tableName, columnDefinition);
            log.info("[{}] Executing ALTER TABLE ADD COLUMN: {}", correlationId, alterSql);
            dslContext.execute(alterSql);

            auditLogger.logColumnAddition(schema, tableName, columnDefinition);
            log.info("[{}] Column {} added successfully", correlationId, columnDefinition.getName());

        } catch (Exception e) {
            log.error("[{}] Error adding column: {}", correlationId, e.getMessage(), e);
            throw new SchemaManagementException(
                    "Failed to add column: " + e.getMessage(), e);
        }
    }

    @Override
    @PerformanceLogger
    public void dropColumn(String schema, String tableName, String columnName) {
        String correlationId = UUID.randomUUID().toString();
        log.info("[{}] Dropping column {} from table {}.{}",
                correlationId, columnName, schema, tableName);

        try {
            validationService.validateIdentifiers(schema, tableName, columnName);

            String alterSql = schemaRepository.buildDropColumnDDL(schema, tableName, columnName);
            log.info("[{}] Executing ALTER TABLE DROP COLUMN: {}", correlationId, alterSql);
            dslContext.execute(alterSql);

            auditLogger.logColumnDrop(schema, tableName, columnName);
            log.info("[{}] Column {} dropped successfully", correlationId, columnName);

        } catch (Exception e) {
            log.error("[{}] Error dropping column: {}", correlationId, e.getMessage(), e);
            throw new SchemaManagementException(
                    "Failed to drop column: " + e.getMessage(), e);
        }
    }

    @Override
    @PerformanceLogger
    public void addConstraint(String schema, String tableName, Constraint constraint) {
        String correlationId = UUID.randomUUID().toString();
        log.info("[{}] Adding constraint to table {}.{}", correlationId, schema, tableName);

        try {
            validationService.validateIdentifiers(schema, tableName);
            validationService.validateConstraint(constraint);

            String constraintSql = buildConstraintDDL(schema, tableName, constraint);
            log.info("[{}] Executing ALTER TABLE ADD CONSTRAINT: {}", correlationId, constraintSql);
            dslContext.execute(constraintSql);

            auditLogger.logConstraintAddition(schema, tableName, constraint);
            log.info("[{}] Constraint added successfully", correlationId);

        } catch (Exception e) {
            log.error("[{}] Error adding constraint: {}", correlationId, e.getMessage(), e);
            throw new ConstraintViolationException("Failed to add constraint: " + e.getMessage(), e);
        }
    }

    @Override
    @PerformanceLogger
    public void dropConstraint(String schema, String tableName, String constraintName) {
        String correlationId = UUID.randomUUID().toString();
        log.info("[{}] Dropping constraint {} from table {}.{}",
                correlationId, constraintName, schema, tableName);

        try {
            validationService.validateIdentifiers(schema, tableName, constraintName);

            String dropSql = schemaRepository.buildDropConstraintDDL(schema, tableName, constraintName);
            log.info("[{}] Executing ALTER TABLE DROP CONSTRAINT: {}", correlationId, dropSql);
            dslContext.execute(dropSql);

            auditLogger.logConstraintDrop(schema, tableName, constraintName);
            log.info("[{}] Constraint {} dropped successfully", correlationId, constraintName);

        } catch (Exception e) {
            log.error("[{}] Error dropping constraint: {}", correlationId, e.getMessage(), e);
            throw new SchemaManagementException(
                    "Failed to drop constraint: " + e.getMessage(), e);
        }
    }

    private void addConstraints(SchemaDefinition schemaDefinition, String correlationId) {
        com.hussain.definition_api.core.model.Constraints constraints = schemaDefinition.getConstraints();

        if (constraints == null) {
            log.debug("[{}] No constraints to add", correlationId);
            return;
        }

        log.debug("[{}] Adding constraints to table", correlationId);

        List<PrimaryKey> primaryKeys = constraints.getPrimaryKeys();
        if (primaryKeys != null && !primaryKeys.isEmpty()) {
            for (PrimaryKey pk : primaryKeys) {
                try {
                    String pkSql = schemaRepository.buildPrimaryKeyDDL(schemaDefinition, pk);
                    log.info("[{}] Adding primary key: {}", correlationId, pkSql);
                    dslContext.execute(pkSql);
                } catch (Exception e) {
                    log.error("[{}] Failed to add primary key: {}", correlationId, e.getMessage());
                    throw e;
                }
            }
        }

        List<UniqueKey> uniqueKeys = constraints.getUniqueKeys();
        if (uniqueKeys != null && !uniqueKeys.isEmpty()) {
            for (UniqueKey uk : uniqueKeys) {
                try {
                    String ukSql = schemaRepository.buildUniqueKeyDDL(schemaDefinition, uk);
                    log.info("[{}] Adding unique key: {}", correlationId, ukSql);
                    dslContext.execute(ukSql);
                } catch (Exception e) {
                    log.error("[{}] Failed to add unique key: {}", correlationId, e.getMessage());
                    throw e;
                }
            }
        }

        List<ForeignKey> foreignKeys = constraints.getForeignKeys();
        if (foreignKeys != null && !foreignKeys.isEmpty()) {
            for (ForeignKey fk : foreignKeys) {
                try {
                    String fkSql = schemaRepository.buildForeignKeyDDL(schemaDefinition, fk);
                    log.info("[{}] Adding foreign key: {}", correlationId, fkSql);
                    dslContext.execute(fkSql);
                } catch (Exception e) {
                    log.error("[{}] Failed to add foreign key: {}", correlationId, e.getMessage());
                    throw e;
                }
            }
        }

        List<CheckConstraint> checkConstraints = constraints.getCheckConstraints();
        if (checkConstraints != null && !checkConstraints.isEmpty()) {
            for (CheckConstraint chk : checkConstraints) {
                try {
                    String chkSql = schemaRepository.buildCheckConstraintDDL(schemaDefinition, chk);
                    log.info("[{}] Adding check constraint: {}", correlationId, chkSql);
                    dslContext.execute(chkSql);
                } catch (Exception e) {
                    log.error("[{}] Failed to add check constraint: {}", correlationId, e.getMessage());
                    throw e;
                }
            }
        }
    }

    private String buildConstraintDDL(String schema, String tableName, Constraint constraint) {
        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE ")
                .append(quoteIdentifier(schema)).append(".")
                .append(quoteIdentifier(tableName))
                .append(" ADD CONSTRAINT ")
                .append(quoteIdentifier(constraint.getName()))
                .append(" ");

        switch (constraint.getType()) {
            case PRIMARY_KEY:
                sql.append("PRIMARY KEY (");
                sql.append(String.join(", ", constraint.getColumns().stream()
                        .map(this::quoteIdentifier)
                        .toArray(String[]::new)));
                sql.append(")");
                break;

            case FOREIGN_KEY:
                sql.append("FOREIGN KEY (");
                sql.append(String.join(", ", constraint.getColumns().stream()
                        .map(this::quoteIdentifier)
                        .toArray(String[]::new)));
                sql.append(") REFERENCES ");
                sql.append(quoteIdentifier(constraint.getReferencedTable()));
                sql.append(" (");
                sql.append(String.join(", ", constraint.getReferencedColumns().stream()
                        .map(this::quoteIdentifier)
                        .toArray(String[]::new)));
                sql.append(")");
                if (constraint.getOnDelete() != null) {
                    sql.append(" ON DELETE ").append(constraint.getOnDelete());
                }
                if (constraint.getOnUpdate() != null) {
                    sql.append(" ON UPDATE ").append(constraint.getOnUpdate());
                }
                break;

            case UNIQUE:
                sql.append("UNIQUE (");
                sql.append(String.join(", ", constraint.getColumns().stream()
                        .map(this::quoteIdentifier)
                        .toArray(String[]::new)));
                sql.append(")");
                break;

            case CHECK:
                sql.append("CHECK (").append(constraint.getExpression()).append(")");
                break;

            default:
                throw new UnsupportedOperationException("Unsupported constraint type: " + constraint.getType());
        }

        return sql.toString();
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return identifier;
        }
        if (identifier.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            return "\"" + identifier + "\"";
        }
        return "\"" + identifier + "\"";
    }
}