package com.hussain.definition_api.core.model;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@Jacksonized
public class SchemaDefinition {

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
    @Singular
    private List<ColumnDefinition> columns;

    @Valid
    private com.hussain.definition_api.core.model.Constraints constraints;

    private String tableComment;
    @Builder.Default
    private boolean ifNotExists = false;
    @Builder.Default
    private boolean temporary = false;

    // Add version field
    private String version;
}