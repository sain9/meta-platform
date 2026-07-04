package com.hussain.definition_api.core.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class CheckConstraint {

    @NotBlank(message = "Check constraint name is required")
    @Size(max = 63, message = "Check constraint name cannot exceed 63 characters")
    @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]*$",
            message = "Check constraint name must start with letter or underscore and contain only alphanumeric characters and underscores")
    private String name;

    @NotBlank(message = "Check constraint expression is required")
    private String expression;
}