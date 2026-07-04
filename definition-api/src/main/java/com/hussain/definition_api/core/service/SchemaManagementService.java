package com.hussain.definition_api.core.service;

import com.hussain.definition_api.core.model.*;

import java.util.List;

public interface SchemaManagementService {

    SchemaCreationResult createTable(SchemaDefinition schemaDefinition);

    SchemaCreationResult createTableWithAudit(SchemaDefinition schemaDefinition, String createdBy);

    // New method to create table and register in entity_store
    SchemaCreationResult createAndRegisterTable(SchemaDefinition schemaDefinition, String createdBy);

    // New method to sync enabled entities on startup
    void syncEnabledEntities();

    void dropTable(String schema, String tableName);

    void dropTableCascade(String schema, String tableName);

    void truncateTable(String schema, String tableName);

    SchemaInfo getTableInfo(String schema, String tableName);

    List<SchemaInfo> listTables(String schema);

    boolean tableExists(String schema, String tableName);

    void addColumn(String schema, String tableName, ColumnDefinition columnDefinition);

    void dropColumn(String schema, String tableName, String columnName);

    void addConstraint(String schema, String tableName, Constraint constraint);

    void dropConstraint(String schema, String tableName, String constraintName);
}