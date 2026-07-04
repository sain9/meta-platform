package com.hussain.definition_api.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class TableOperationRequest {

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

    private boolean cascade;
}