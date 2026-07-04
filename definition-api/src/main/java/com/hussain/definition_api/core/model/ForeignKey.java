package com.hussain.definition_api.core.model;

import com.hussain.definition_api.core.enums.ReferentialAction;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@Jacksonized
public class ForeignKey {

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
    @Builder.Default
    private boolean deferrable = false;
    @Builder.Default
    private boolean initiallyDeferred = false;
    @Builder.Default
    private boolean matchFull = false;
    @Builder.Default
    private boolean matchPartial = false;
    @Builder.Default
    private boolean matchSimple = true;
}