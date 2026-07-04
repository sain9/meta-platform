package com.hussain.definition_api.core.service.impl;

import com.hussain.definition_api.core.model.SchemaInfo;
import com.hussain.definition_api.core.service.SchemaMetadataService;
import com.hussain.definition_api.core.service.SchemaValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchemaMetadataServiceImpl implements SchemaMetadataService {

    private final DSLContext dslContext;
    private final SchemaValidationService validationService;

    @Override
    public SchemaInfo getTableInfo(String schema, String tableName) {
        validationService.validateIdentifiers(schema, tableName);
        log.debug("Fetching metadata for table: {}.{}", schema, tableName);

        // Check if table exists
        String checkSql = "SELECT COUNT(*) FROM information_schema.tables " +
                         "WHERE table_schema = ? AND table_name = ?";
        Integer count = dslContext.fetchOne(checkSql, schema, tableName)
                .into(Integer.class);

        if (count == null || count == 0) {
            throw new RuntimeException(String.format("Table %s.%s does not exist", schema, tableName));
        }

        // Get table comment
        String commentSql = "SELECT obj_description('%s.%s'::regclass)";
        String comment = dslContext.fetchOne(
            String.format(commentSql, schema, tableName)
        ).into(String.class);

        // Get columns
        String columnSql = """
            SELECT 
                column_name,
                data_type,
                is_nullable,
                column_default,
                character_maximum_length,
                numeric_precision,
                numeric_scale
            FROM information_schema.columns
            WHERE table_schema = ? AND table_name = ?
            ORDER BY ordinal_position
            """;

        var columnRecords = dslContext.fetch(columnSql, schema, tableName);
        List<SchemaInfo.ColumnInfo> columns = new ArrayList<>();

        for (var record : columnRecords) {
            columns.add(SchemaInfo.ColumnInfo.builder()
                .name(record.get("column_name", String.class))
                .type(record.get("data_type", String.class))
                .nullable("YES".equals(record.get("is_nullable", String.class)))
                .defaultValue(record.get("column_default", String.class))
                .comment(null) // Would need to fetch from pg_description
                .build());
        }

        return SchemaInfo.builder()
                .schema(schema)
                .tableName(tableName)
                .tableType("BASE TABLE")
                .tableComment(comment)
                .columns(columns)
                .build();
    }

    @Override
    public List<SchemaInfo> listTables(String schema) {
        validationService.validateSchemaName(schema);
        log.debug("Listing tables in schema: {}", schema);

        String sql = """
            SELECT 
                table_name,
                table_type
            FROM information_schema.tables
            WHERE table_schema = ? 
            AND table_type = 'BASE TABLE'
            ORDER BY table_name
            """;

        var records = dslContext.fetch(sql, schema);
        List<SchemaInfo> tables = new ArrayList<>();

        for (var record : records) {
            String tableName = record.get("table_name", String.class);
            tables.add(SchemaInfo.builder()
                .schema(schema)
                .tableName(tableName)
                .tableType(record.get("table_type", String.class))
                .build());
        }

        return tables;
    }

    @Override
    public List<String> listSchemas() {
        log.debug("Listing all schemas");

        String sql = """
            SELECT schema_name
            FROM information_schema.schemata
            WHERE schema_name NOT IN ('information_schema', 'pg_catalog', 'pg_toast')
            AND schema_name NOT LIKE 'pg_%'
            ORDER BY schema_name
            """;

        return dslContext.fetch(sql)
                .map(record -> record.get("schema_name", String.class));
    }
}