package com.hussain.definition_api.core.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
public class SchemaInfo {
    private String schema;
    private String tableName;
    private String tableType;
    private String tableComment;
    private List<ColumnInfo> columns;
    private List<ConstraintInfo> constraints;

    @Data
    @Builder
    @Jacksonized
    public static class ColumnInfo {
        private String name;
        private String type;
        private boolean nullable;
        private String defaultValue;
        private boolean isPrimaryKey;
        private String comment;
    }

    @Data
    @Builder
    @Jacksonized
    public static class ConstraintInfo {
        private String name;
        private String type;
        private String definition;
        private List<String> columns;
    }
}