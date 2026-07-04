package com.hussain.definition_api.core.model;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Jacksonized
public class Constraints {

    @Valid
    @Singular
    private List<PrimaryKey> primaryKeys;

    @Valid
    @Singular
    private List<UniqueKey> uniqueKeys;

    @Valid
    @Singular
    private List<ForeignKey> foreignKeys;

    @Valid
    @Singular
    private List<CheckConstraint> checkConstraints;

    // Ensure lists are never null
    public List<PrimaryKey> getPrimaryKeys() {
        return primaryKeys != null ? primaryKeys : new ArrayList<>();
    }

    public List<UniqueKey> getUniqueKeys() {
        return uniqueKeys != null ? uniqueKeys : new ArrayList<>();
    }

    public List<ForeignKey> getForeignKeys() {
        return foreignKeys != null ? foreignKeys : new ArrayList<>();
    }

    public List<CheckConstraint> getCheckConstraints() {
        return checkConstraints != null ? checkConstraints : new ArrayList<>();
    }
}