package com.hussain.definition_api.core.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
public class UniqueKey {

    @NotBlank(message = "Unique key name is required")
    @Size(max = 63, message = "Unique key name cannot exceed 63 characters")
    @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]*$",
            message = "Unique key name must start with letter or underscore and contain only alphanumeric characters and underscores")
    private String name;

    @NotEmpty(message = "Unique key columns are required")
    private List<String> columns;
}