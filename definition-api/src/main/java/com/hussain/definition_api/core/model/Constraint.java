package com.hussain.definition_api.core.model;

import com.hussain.definition_api.core.enums.ConstraintType;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@Builder
@Jacksonized
public class Constraint {
    
    @NotBlank(message = "Constraint name is required")
    @Size(max = 63, message = "Constraint name cannot exceed 63 characters")
    private String name;
    
    @NotNull(message = "Constraint type is required")
    private ConstraintType type;
    
    private List<String> columns;
    private String referencedTable;
    private List<String> referencedColumns;
    private String expression;
    private String onDelete;
    private String onUpdate;
    private boolean deferrable;
    private boolean initiallyDeferred;
}