package com.hussain.definition_api.infrastructure.repository.impl;

import com.hussain.definition_api.core.enums.ColumnType;
import com.hussain.definition_api.core.model.*;
import com.hussain.definition_api.infrastructure.repository.SchemaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SchemaRepositoryImpl implements SchemaRepository {

    private final DSLContext dslContext;

    @Override
    public List<String> buildCreateTableDDL(SchemaDefinition schemaDefinition) {
        List<String> ddlStatements = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE ");
        if (schemaDefinition.isTemporary()) {
            sql.append("TEMP ");
        }
        sql.append("TABLE ");

        if (schemaDefinition.isIfNotExists()) {
            sql.append("IF NOT EXISTS ");
        }

        sql.append(quoteIdentifier(schemaDefinition.getSchema()))
                .append(".")
                .append(quoteIdentifier(schemaDefinition.getTableName()))
                .append(" (\n");

        List<String> columnDefs = new ArrayList<>();
        for (com.hussain.definition_api.core.model.ColumnDefinition col : schemaDefinition.getColumns()) {
            columnDefs.add(buildColumnDefinition(col));
        }

        sql.append(String.join(",\n", columnDefs));
        sql.append("\n)");

        ddlStatements.add(sql.toString());
        return ddlStatements;
    }

    @Override
    public String buildDropTableDDL(String schema, String tableName, boolean cascade) {
        StringBuilder sql = new StringBuilder();
        sql.append("DROP TABLE IF EXISTS ")
                .append(quoteIdentifier(schema))
                .append(".")
                .append(quoteIdentifier(tableName));

        if (cascade) {
            sql.append(" CASCADE");
        }

        return sql.toString();
    }

    @Override
    public String buildTruncateTableDDL(String schema, String tableName) {
        return "TRUNCATE TABLE " +
                quoteIdentifier(schema) + "." +
                quoteIdentifier(tableName) +
                " RESTART IDENTITY";
    }

    @Override
    public String buildAddColumnDDL(String schema, String tableName, com.hussain.definition_api.core.model.ColumnDefinition column) {
        return "ALTER TABLE " +
                quoteIdentifier(schema) + "." + quoteIdentifier(tableName) +
                " ADD COLUMN " + buildColumnDefinition(column);
    }

    @Override
    public String buildDropColumnDDL(String schema, String tableName, String columnName) {
        return "ALTER TABLE " +
                quoteIdentifier(schema) + "." + quoteIdentifier(tableName) +
                " DROP COLUMN " + quoteIdentifier(columnName);
    }

    @Override
    public String buildPrimaryKeyDDL(SchemaDefinition schemaDef, PrimaryKey pk) {
        String columns = pk.getColumns().stream()
                .map(this::quoteIdentifier)
                .collect(Collectors.joining(", "));

        return "ALTER TABLE " +
                quoteIdentifier(schemaDef.getSchema()) + "." +
                quoteIdentifier(schemaDef.getTableName()) +
                " ADD CONSTRAINT " + quoteIdentifier(pk.getName()) +
                " PRIMARY KEY (" + columns + ")";
    }

    @Override
    public String buildUniqueKeyDDL(SchemaDefinition schemaDef, UniqueKey uk) {
        String columns = uk.getColumns().stream()
                .map(this::quoteIdentifier)
                .collect(Collectors.joining(", "));

        return "ALTER TABLE " +
                quoteIdentifier(schemaDef.getSchema()) + "." +
                quoteIdentifier(schemaDef.getTableName()) +
                " ADD CONSTRAINT " + quoteIdentifier(uk.getName()) +
                " UNIQUE (" + columns + ")";
    }

    @Override
    public String buildForeignKeyDDL(SchemaDefinition schemaDef, ForeignKey fk) {
        String columns = fk.getColumns().stream()
                .map(this::quoteIdentifier)
                .collect(Collectors.joining(", "));

        String refColumns = fk.getReferencedColumns().stream()
                .map(this::quoteIdentifier)
                .collect(Collectors.joining(", "));

        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE ")
                .append(quoteIdentifier(schemaDef.getSchema())).append(".")
                .append(quoteIdentifier(schemaDef.getTableName()))
                .append(" ADD CONSTRAINT ")
                .append(quoteIdentifier(fk.getName()))
                .append(" FOREIGN KEY (")
                .append(columns)
                .append(") REFERENCES ")
                .append(quoteIdentifier(schemaDef.getSchema())).append(".")
                .append(quoteIdentifier(fk.getReferencedTable()))
                .append(" (")
                .append(refColumns)
                .append(")");

        if (fk.getOnDelete() != null) {
            sql.append(" ON DELETE ").append(fk.getOnDelete().getSqlKeyword());
        }

        if (fk.getOnUpdate() != null) {
            sql.append(" ON UPDATE ").append(fk.getOnUpdate().getSqlKeyword());
        }

        if (fk.isDeferrable()) {
            sql.append(" DEFERRABLE");
            if (fk.isInitiallyDeferred()) {
                sql.append(" INITIALLY DEFERRED");
            }
        }

        if (fk.isMatchFull()) {
            sql.append(" MATCH FULL");
        } else if (fk.isMatchPartial()) {
            sql.append(" MATCH PARTIAL");
        } else if (fk.isMatchSimple()) {
            sql.append(" MATCH SIMPLE");
        }

        return sql.toString();
    }

    @Override
    public String buildCheckConstraintDDL(SchemaDefinition schemaDef, CheckConstraint chk) {
        return "ALTER TABLE " +
                quoteIdentifier(schemaDef.getSchema()) + "." +
                quoteIdentifier(schemaDef.getTableName()) +
                " ADD CONSTRAINT " + quoteIdentifier(chk.getName()) +
                " CHECK (" + chk.getExpression() + ")";
    }

    @Override
    public String buildAddConstraintDDL(String schema, String tableName, Constraint constraint) {
        throw new UnsupportedOperationException("Use specific constraint methods instead");
    }

    @Override
    public String buildDropConstraintDDL(String schema, String tableName, String constraintName) {
        return "ALTER TABLE " +
                quoteIdentifier(schema) + "." + quoteIdentifier(tableName) +
                " DROP CONSTRAINT " + quoteIdentifier(constraintName);
    }

    @Override
    public String buildTableCommentDDL(SchemaDefinition schemaDefinition) {
        return "COMMENT ON TABLE " +
                quoteIdentifier(schemaDefinition.getSchema()) + "." +
                quoteIdentifier(schemaDefinition.getTableName()) +
                " IS '" + schemaDefinition.getTableComment().replace("'", "''") + "'";
    }

    @Override
    public boolean tableExists(String schema, String tableName) {
        String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = ? AND table_name = ?";

        Integer count = dslContext.fetchOne(sql, schema, tableName)
                .into(Integer.class);

        return count != null && count > 0;
    }

    @Override
    public TableMetadata getTableMetadata(String schema, String tableName) {
        throw new UnsupportedOperationException("Implement table metadata fetching");
    }

    /**
     * Builds the column definition SQL with proper handling of default values
     */
    private String buildColumnDefinition(com.hussain.definition_api.core.model.ColumnDefinition col) {
        StringBuilder def = new StringBuilder();
        def.append(quoteIdentifier(col.getName()))
                .append(" ")
                .append(col.getType().getSqlType());

        // Add length for VARCHAR and CHAR
        if ((col.getType() == ColumnType.VARCHAR || col.getType() == ColumnType.CHAR)
                && col.getLength() != null && col.getLength() > 0) {
            def.append("(").append(col.getLength()).append(")");
        }

        // Add precision and scale for DECIMAL/NUMERIC
        if ((col.getType() == ColumnType.DECIMAL || col.getType() == ColumnType.NUMERIC)
                && col.getPrecision() != null) {
            def.append("(").append(col.getPrecision());
            if (col.getScale() != null) {
                def.append(", ").append(col.getScale());
            }
            def.append(")");
        }

        // Add IDENTITY if specified
        if (col.isIdentity()) {
            def.append(" GENERATED BY DEFAULT AS IDENTITY");
            if (col.getIdentityStartWith() != null) {
                def.append(" (START WITH ").append(col.getIdentityStartWith()).append(")");
            }
            if (col.getIdentityIncrementBy() != null) {
                def.append(" INCREMENT BY ").append(col.getIdentityIncrementBy());
            }
        }

        // Add GENERATED if specified
        if (col.isGenerated() && col.getExpression() != null) {
            def.append(" GENERATED ALWAYS AS (").append(col.getExpression()).append(") STORED");
        }

        // Add NOT NULL constraint
        if (!col.isNullable()) {
            def.append(" NOT NULL");
        }

        // Add UNIQUE constraint
        if (col.getUnique() != null && col.getUnique()) {
            def.append(" UNIQUE");
        }

        // Add DEFAULT value with proper handling for functions vs literals
        if (col.getDefaultValue() != null) {
            def.append(" DEFAULT ");
            String defaultValue = col.getDefaultValue().toString();

            // Check if it's a PostgreSQL function that shouldn't be quoted
            if (isPostgreSQLFunction(defaultValue)) {
                def.append(defaultValue);  // No quotes for functions
            } else {
                // For string literals, add quotes and escape single quotes
                String escaped = defaultValue.replace("'", "''");
                def.append("'").append(escaped).append("'");
            }
        }

        return def.toString();
    }

    /**
     * Checks if a value is a PostgreSQL function that should not be quoted
     */
    private boolean isPostgreSQLFunction(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        String upper = value.toUpperCase().trim();

        // Common PostgreSQL functions that can be used as default values
        String[] functions = {
                // Date/Time functions
                "CURRENT_TIMESTAMP", "CURRENT_DATE", "CURRENT_TIME",
                "NOW()", "LOCALTIMESTAMP", "LOCALTIME",
                "CLOCK_TIMESTAMP()", "TRANSACTION_TIMESTAMP()",
                "STATEMENT_TIMESTAMP()", "TIMEOFDAY()",

                // User/Session functions
                "CURRENT_USER", "SESSION_USER", "USER",

                // UUID functions
                "UUID_GENERATE_V4()", "GEN_RANDOM_UUID()",

                // Other functions
                "RANDOM()", "GEN_SALT()", "CRYPT()",

                // JSON functions
                "JSON_BUILD_OBJECT()", "JSONB_BUILD_OBJECT()",

                // Array functions
                "ARRAY[]",

                // System functions
                "VERSION()"
        };

        for (String func : functions) {
            if (upper.equals(func) || upper.startsWith(func)) {
                return true;
            }
        }

        // Check for common patterns
        // Functions with parentheses: FUNCTION_NAME()
        if (upper.matches("^[A-Z_]+\\()")) {
            return true;
        }

        return false;
    }

    private String quoteIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return identifier;
        }
        // Only quote if it contains special characters or is a reserved word
        if (identifier.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            return "\"" + identifier + "\"";
        }
        return "\"" + identifier + "\"";
    }
}