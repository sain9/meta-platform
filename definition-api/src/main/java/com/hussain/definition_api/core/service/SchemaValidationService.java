package com.hussain.definition_api.core.service;

import com.hussain.definition_api.core.model.*;

public interface SchemaValidationService {

    void validateSchemaDefinition(SchemaDefinition schemaDefinition);

    void validateColumnDefinition(ColumnDefinition columnDefinition);

    void validateConstraint(Constraint constraint);

    void validateIdentifiers(String... identifiers);

    void validateSchemaName(String schema);

    void validateTableName(String tableName);

    void validateColumnName(String columnName);

    void validatePrimaryKey(PrimaryKey primaryKey);

    void validateForeignKey(ForeignKey foreignKey);

    void validateUniqueKey(UniqueKey uniqueKey);

    void validateCheckConstraint(CheckConstraint checkConstraint);
}