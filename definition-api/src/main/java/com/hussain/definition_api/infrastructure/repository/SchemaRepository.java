package com.hussain.definition_api.infrastructure.repository;

import com.hussain.definition_api.core.model.*;

import java.util.List;

public interface SchemaRepository {

    List<String> buildCreateTableDDL(SchemaDefinition schemaDefinition);

    String buildDropTableDDL(String schema, String tableName, boolean cascade);

    String buildTruncateTableDDL(String schema, String tableName);

    String buildAddColumnDDL(String schema, String tableName, com.hussain.definition_api.core.model.ColumnDefinition column);

    String buildDropColumnDDL(String schema, String tableName, String columnName);

    String buildPrimaryKeyDDL(SchemaDefinition schemaDef, PrimaryKey pk);

    String buildUniqueKeyDDL(SchemaDefinition schemaDef, UniqueKey uk);

    String buildForeignKeyDDL(SchemaDefinition schemaDef, ForeignKey fk);

    String buildCheckConstraintDDL(SchemaDefinition schemaDef, CheckConstraint chk);

    String buildAddConstraintDDL(String schema, String tableName, Constraint constraint);

    String buildDropConstraintDDL(String schema, String tableName, String constraintName);

    String buildTableCommentDDL(SchemaDefinition schemaDefinition);

    boolean tableExists(String schema, String tableName);

    TableMetadata getTableMetadata(String schema, String tableName);
}