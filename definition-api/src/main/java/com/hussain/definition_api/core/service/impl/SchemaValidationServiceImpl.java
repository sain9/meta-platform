package com.hussain.definition_api.core.service.impl;

import com.hussain.definition_api.core.exception.InvalidSchemaDefinitionException;
import com.hussain.definition_api.core.model.*;
import com.hussain.definition_api.core.service.SchemaValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Service
public class SchemaValidationServiceImpl implements SchemaValidationService {

    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
    private static final int MAX_IDENTIFIER_LENGTH = 63;

    @Override
    public void validateSchemaDefinition(SchemaDefinition schemaDefinition) {
        if (schemaDefinition == null) {
            throw new InvalidSchemaDefinitionException("Schema definition cannot be null");
        }

        validateSchemaName(schemaDefinition.getSchema());
        validateTableName(schemaDefinition.getTableName());

        if (schemaDefinition.getColumns() == null || schemaDefinition.getColumns().isEmpty()) {
            throw new InvalidSchemaDefinitionException("At least one column is required");
        }

        // Check for duplicate column names
        Set<String> columnNames = new HashSet<>();
        for (ColumnDefinition col : schemaDefinition.getColumns()) {
            validateColumnDefinition(col);
            if (!columnNames.add(col.getName().toLowerCase())) {
                throw new InvalidSchemaDefinitionException(
                    String.format("Duplicate column name: %s", col.getName())
                );
            }
        }

        // Validate constraints if present
        Constraints constraints = schemaDefinition.getConstraints();
        if (constraints != null) {
            validateConstraints(constraints, schemaDefinition.getTableName());
        }

        log.debug("Schema definition validation passed for table: {}.{}",
            schemaDefinition.getSchema(), schemaDefinition.getTableName());
    }

    @Override
    public void validateColumnDefinition(ColumnDefinition columnDefinition) {
        if (columnDefinition == null) {
            throw new InvalidSchemaDefinitionException("Column definition cannot be null");
        }

        validateColumnName(columnDefinition.getName());

        if (columnDefinition.getType() == null) {
            throw new InvalidSchemaDefinitionException(
                String.format("Column type is required for column: %s", columnDefinition.getName())
            );
        }

        // Validate length for VARCHAR and CHAR
        if ((columnDefinition.getType() == com.hussain.definition_api.core.enums.ColumnType.VARCHAR ||
             columnDefinition.getType() == com.hussain.definition_api.core.enums.ColumnType.CHAR) &&
            columnDefinition.getLength() != null &&
            (columnDefinition.getLength() < 1 || columnDefinition.getLength() > 10485760)) {
            throw new InvalidSchemaDefinitionException(
                String.format("Invalid length %d for column: %s. Length must be between 1 and 10485760",
                    columnDefinition.getLength(), columnDefinition.getName())
            );
        }

        // Validate precision for DECIMAL/NUMERIC
        if ((columnDefinition.getType() == com.hussain.definition_api.core.enums.ColumnType.DECIMAL ||
             columnDefinition.getType() == com.hussain.definition_api.core.enums.ColumnType.NUMERIC) &&
            columnDefinition.getPrecision() != null &&
            (columnDefinition.getPrecision() < 1 || columnDefinition.getPrecision() > 1000)) {
            throw new InvalidSchemaDefinitionException(
                String.format("Invalid precision %d for column: %s. Precision must be between 1 and 1000",
                    columnDefinition.getPrecision(), columnDefinition.getName())
            );
        }

        // Validate generated columns
        if (columnDefinition.isGenerated() && columnDefinition.getExpression() == null) {
            throw new InvalidSchemaDefinitionException(
                String.format("Expression is required for generated column: %s", columnDefinition.getName())
            );
        }

        log.debug("Column validation passed for: {}", columnDefinition.getName());
    }

    @Override
    public void validateConstraint(Constraint constraint) {
        if (constraint == null) {
            throw new InvalidSchemaDefinitionException("Constraint cannot be null");
        }

        if (constraint.getName() == null || constraint.getName().trim().isEmpty()) {
            throw new InvalidSchemaDefinitionException("Constraint name is required");
        }

        if (constraint.getName().length() > MAX_IDENTIFIER_LENGTH) {
            throw new InvalidSchemaDefinitionException(
                String.format("Constraint name too long: %s. Maximum length is %d",
                    constraint.getName(), MAX_IDENTIFIER_LENGTH)
            );
        }

        if (!IDENTIFIER_PATTERN.matcher(constraint.getName()).matches()) {
            throw new InvalidSchemaDefinitionException(
                String.format("Invalid constraint name: %s. Must start with letter or underscore and contain only alphanumeric characters",
                    constraint.getName())
            );
        }

        if (constraint.getType() == null) {
            throw new InvalidSchemaDefinitionException(
                String.format("Constraint type is required for: %s", constraint.getName())
            );
        }

        log.debug("Constraint validation passed for: {}", constraint.getName());
    }

    @Override
    public void validateIdentifiers(String... identifiers) {
        for (String identifier : identifiers) {
            if (identifier == null || identifier.trim().isEmpty()) {
                throw new InvalidSchemaDefinitionException("Identifier cannot be null or empty");
            }

            if (identifier.length() > MAX_IDENTIFIER_LENGTH) {
                throw new InvalidSchemaDefinitionException(
                    String.format("Identifier too long: %s. Maximum length is %d",
                        identifier, MAX_IDENTIFIER_LENGTH)
                );
            }

            if (!IDENTIFIER_PATTERN.matcher(identifier).matches()) {
                throw new InvalidSchemaDefinitionException(
                    String.format("Invalid identifier: %s. Must start with letter or underscore and contain only alphanumeric characters",
                        identifier)
                );
            }
        }
    }

    @Override
    public void validateSchemaName(String schema) {
        validateIdentifiers(schema);
        // Additional schema-specific validations
        if (schema.equalsIgnoreCase("information_schema") ||
            schema.equalsIgnoreCase("pg_catalog") ||
            schema.equalsIgnoreCase("pg_toast")) {
            throw new InvalidSchemaDefinitionException(
                String.format("Cannot use system schema: %s", schema)
            );
        }
    }

    @Override
    public void validateTableName(String tableName) {
        validateIdentifiers(tableName);
        // Additional table-specific validations
        if (tableName.toLowerCase().startsWith("pg_")) {
            throw new InvalidSchemaDefinitionException(
                String.format("Table name cannot start with 'pg_': %s", tableName)
            );
        }
    }

    @Override
    public void validateColumnName(String columnName) {
        validateIdentifiers(columnName);
    }

    @Override
    public void validatePrimaryKey(PrimaryKey primaryKey) {
        if (primaryKey == null) {
            throw new InvalidSchemaDefinitionException("Primary key cannot be null");
        }

        validateConstraint(Constraint.builder()
            .name(primaryKey.getName())
            .type(com.hussain.definition_api.core.enums.ConstraintType.PRIMARY_KEY)
            .build());

        if (primaryKey.getColumns() == null || primaryKey.getColumns().isEmpty()) {
            throw new InvalidSchemaDefinitionException(
                String.format("At least one column is required for primary key: %s", primaryKey.getName())
            );
        }

        for (String column : primaryKey.getColumns()) {
            validateColumnName(column);
        }
    }

    @Override
    public void validateForeignKey(ForeignKey foreignKey) {
        if (foreignKey == null) {
            throw new InvalidSchemaDefinitionException("Foreign key cannot be null");
        }

        validateConstraint(Constraint.builder()
            .name(foreignKey.getName())
            .type(com.hussain.definition_api.core.enums.ConstraintType.FOREIGN_KEY)
            .build());

        if (foreignKey.getColumns() == null || foreignKey.getColumns().isEmpty()) {
            throw new InvalidSchemaDefinitionException(
                String.format("At least one column is required for foreign key: %s", foreignKey.getName())
            );
        }

        if (foreignKey.getReferencedTable() == null || foreignKey.getReferencedTable().trim().isEmpty()) {
            throw new InvalidSchemaDefinitionException(
                String.format("Referenced table is required for foreign key: %s", foreignKey.getName())
            );
        }

        if (foreignKey.getReferencedColumns() == null || foreignKey.getReferencedColumns().isEmpty()) {
            throw new InvalidSchemaDefinitionException(
                String.format("Referenced columns are required for foreign key: %s", foreignKey.getName())
            );
        }

        if (foreignKey.getColumns().size() != foreignKey.getReferencedColumns().size()) {
            throw new InvalidSchemaDefinitionException(
                String.format("Column count mismatch for foreign key: %s. Columns: %d, Referenced columns: %d",
                    foreignKey.getName(), foreignKey.getColumns().size(), foreignKey.getReferencedColumns().size())
            );
        }

        for (String column : foreignKey.getColumns()) {
            validateColumnName(column);
        }

        validateTableName(foreignKey.getReferencedTable());
    }

    @Override
    public void validateUniqueKey(UniqueKey uniqueKey) {
        if (uniqueKey == null) {
            throw new InvalidSchemaDefinitionException("Unique key cannot be null");
        }

        validateConstraint(Constraint.builder()
            .name(uniqueKey.getName())
            .type(com.hussain.definition_api.core.enums.ConstraintType.UNIQUE)
            .build());

        if (uniqueKey.getColumns() == null || uniqueKey.getColumns().isEmpty()) {
            throw new InvalidSchemaDefinitionException(
                String.format("At least one column is required for unique key: %s", uniqueKey.getName())
            );
        }

        for (String column : uniqueKey.getColumns()) {
            validateColumnName(column);
        }
    }

    @Override
    public void validateCheckConstraint(CheckConstraint checkConstraint) {
        if (checkConstraint == null) {
            throw new InvalidSchemaDefinitionException("Check constraint cannot be null");
        }

        validateConstraint(Constraint.builder()
            .name(checkConstraint.getName())
            .type(com.hussain.definition_api.core.enums.ConstraintType.CHECK)
            .build());

        if (checkConstraint.getExpression() == null || checkConstraint.getExpression().trim().isEmpty()) {
            throw new InvalidSchemaDefinitionException(
                String.format("Expression is required for check constraint: %s", checkConstraint.getName())
            );
        }
    }

    private void validateConstraints(Constraints constraints, String tableName) {
        // Validate Primary Keys
        if (constraints.getPrimaryKeys() != null) {
            for (PrimaryKey pk : constraints.getPrimaryKeys()) {
                validatePrimaryKey(pk);
            }
        }

        // Validate Unique Keys
        if (constraints.getUniqueKeys() != null) {
            for (UniqueKey uk : constraints.getUniqueKeys()) {
                validateUniqueKey(uk);
            }
        }

        // Validate Foreign Keys
        if (constraints.getForeignKeys() != null) {
            for (ForeignKey fk : constraints.getForeignKeys()) {
                validateForeignKey(fk);
            }
        }

        // Validate Check Constraints
        if (constraints.getCheckConstraints() != null) {
            for (CheckConstraint chk : constraints.getCheckConstraints()) {
                validateCheckConstraint(chk);
            }
        }

        // Check for duplicate constraint names
        Set<String> constraintNames = new HashSet<>();
        
        if (constraints.getPrimaryKeys() != null) {
            for (PrimaryKey pk : constraints.getPrimaryKeys()) {
                if (!constraintNames.add(pk.getName().toLowerCase())) {
                    throw new InvalidSchemaDefinitionException(
                        String.format("Duplicate constraint name: %s", pk.getName())
                    );
                }
            }
        }

        if (constraints.getUniqueKeys() != null) {
            for (UniqueKey uk : constraints.getUniqueKeys()) {
                if (!constraintNames.add(uk.getName().toLowerCase())) {
                    throw new InvalidSchemaDefinitionException(
                        String.format("Duplicate constraint name: %s", uk.getName())
                    );
                }
            }
        }

        if (constraints.getForeignKeys() != null) {
            for (ForeignKey fk : constraints.getForeignKeys()) {
                if (!constraintNames.add(fk.getName().toLowerCase())) {
                    throw new InvalidSchemaDefinitionException(
                        String.format("Duplicate constraint name: %s", fk.getName())
                    );
                }
            }
        }

        if (constraints.getCheckConstraints() != null) {
            for (CheckConstraint chk : constraints.getCheckConstraints()) {
                if (!constraintNames.add(chk.getName().toLowerCase())) {
                    throw new InvalidSchemaDefinitionException(
                        String.format("Duplicate constraint name: %s", chk.getName())
                    );
                }
            }
        }
    }
}