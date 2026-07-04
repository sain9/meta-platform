package com.hussain.definition_api.core.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;

@Data
@Builder
@Jacksonized
public class TableMetadata {
    private String schema;
    private String tableName;
    private String tableType;
    private String tableComment;
    private List<ColumnMetadata> columns;
    private List<ConstraintMetadata> constraints;
    private Map<String, String> options;

    @Data
    @Builder
    @Jacksonized
    public static class ColumnMetadata {
        private String name;
        private String dataType;
        private boolean nullable;
        private Object defaultValue;
        private boolean isPrimaryKey;
        private String comment;
        private Integer length;
        private Integer precision;
        private Integer scale;
    }

    @Data
    @Builder
    @Jacksonized
    public static class ConstraintMetadata {
        private String name;
        private String type;
        private String definition;
        private List<String> columns;
        private String referencedTable;
        private List<String> referencedColumns;
    }
}