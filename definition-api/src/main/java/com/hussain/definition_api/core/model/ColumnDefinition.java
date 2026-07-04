package com.hussain.definition_api.core.model;

import com.hussain.definition_api.core.enums.ColumnType;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@Builder
@Jacksonized
public class ColumnDefinition {

    @NotBlank(message = "Column name is required")
    @Size(max = 63, message = "Column name cannot exceed 63 characters")
    @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]*$",
            message = "Column name must start with letter or underscore and contain only alphanumeric characters and underscores")
    private String name;

    @NotNull(message = "Column type is required")
    private ColumnType type;

    private Integer length;
    private Integer precision;
    private Integer scale;

    @Builder.Default
    private boolean nullable = true;

    private Boolean unique;
    private Object defaultValue;
    private String comment;
    private String expression;
    @Builder.Default
    private boolean generated = false;
    @Builder.Default
    private boolean identity = false;
    private Integer identityStartWith;
    private Integer identityIncrementBy;
}