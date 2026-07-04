package com.hussain.definition_api.api.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hussain.definition_api.core.enums.ColumnType;
import com.hussain.definition_api.core.enums.ReferentialAction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SchemaCreationRequest {

    @NotBlank(message = "Schema name is required")
    @Size(max = 63, message = "Schema name cannot exceed 63 characters")
    @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]*$",
            message = "Schema name must start with letter or underscore and contain only alphanumeric characters and underscores")
    private String schema;

    @NotBlank(message = "Table name is required")
    @Size(max = 63, message = "Table name cannot exceed 63 characters")
    @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]*$",
            message = "Table name must start with letter or underscore and contain only alphanumeric characters and underscores")
    private String tableName;

    @NotEmpty(message = "At least one column is required")
    @Valid
    private List<ColumnRequest> columns;

    @Valid
    private ConstraintsRequest constraints;

    private String tableComment;
    private boolean ifNotExists;
    private boolean temporary;

    // Add version field
    private String version;

    @Data
    @Builder
    @Jacksonized
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ColumnRequest {
        @NotBlank(message = "Column name is required")
        @Size(max = 63, message = "Column name cannot exceed 63 characters")
        @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]*$",
                message = "Column name must start with letter or underscore and contain only alphanumeric characters and underscores")
        private String name;

        private ColumnType type;

        private Integer length;
        private Integer precision;
        private Integer scale;
        private boolean nullable = true;
        private Boolean unique;
        private Object defaultValue;
        private String comment;
        private String expression;
        private boolean generated;
        private boolean identity;
        private Integer identityStartWith;
        private Integer identityIncrementBy;
    }

    @Data
    @Builder
    @Jacksonized
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConstraintsRequest {
        @Valid
        private List<PrimaryKeyRequest> primaryKeys;

        @Valid
        private List<UniqueKeyRequest> uniqueKeys;

        @Valid
        private List<ForeignKeyRequest> foreignKeys;

        @Valid
        private List<CheckConstraintRequest> checkConstraints;

        @Data
        @Builder
        @Jacksonized
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class PrimaryKeyRequest {
            @NotBlank(message = "Primary key name is required")
            @Size(max = 63, message = "Primary key name cannot exceed 63 characters")
            @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]*$",
                    message = "Primary key name must start with letter or underscore and contain only alphanumeric characters and underscores")
            private String name;

            @NotEmpty(message = "Primary key columns are required")
            private List<String> columns;
        }

        @Data
        @Builder
        @Jacksonized
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class UniqueKeyRequest {
            @NotBlank(message = "Unique key name is required")
            @Size(max = 63, message = "Unique key name cannot exceed 63 characters")
            @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]*$",
                    message = "Unique key name must start with letter or underscore and contain only alphanumeric characters and underscores")
            private String name;

            @NotEmpty(message = "Unique key columns are required")
            private List<String> columns;
        }

        @Data
        @Builder
        @Jacksonized
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ForeignKeyRequest {
            @NotBlank(message = "Foreign key name is required")
            @Size(max = 63, message = "Foreign key name cannot exceed 63 characters")
            @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]*$",
                    message = "Foreign key name must start with letter or underscore and contain only alphanumeric characters and underscores")
            private String name;

            @NotEmpty(message = "Foreign key columns are required")
            private List<String> columns;

            @NotBlank(message = "Referenced table is required")
            private String referencedTable;

            @NotEmpty(message = "Referenced columns are required")
            private List<String> referencedColumns;

            private ReferentialAction onDelete;
            private ReferentialAction onUpdate;
            private boolean deferrable;
            private boolean initiallyDeferred;
            private boolean matchFull;
            private boolean matchPartial;
            private boolean matchSimple = true;
        }

        @Data
        @Builder
        @Jacksonized
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class CheckConstraintRequest {
            @NotBlank(message = "Check constraint name is required")
            @Size(max = 63, message = "Check constraint name cannot exceed 63 characters")
            @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]*$",
                    message = "Check constraint name must start with letter or underscore and contain only alphanumeric characters and underscores")
            private String name;

            @NotBlank(message = "Check constraint expression is required")
            private String expression;
        }
    }
}