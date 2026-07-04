package com.hussain.definition_api.core.enums;

public enum ConstraintType {
    PRIMARY_KEY("PRIMARY KEY"),
    FOREIGN_KEY("FOREIGN KEY"),
    UNIQUE("UNIQUE"),
    CHECK("CHECK"),
    EXCLUSION("EXCLUSION");

    private final String sqlKeyword;

    ConstraintType(String sqlKeyword) {
        this.sqlKeyword = sqlKeyword;
    }

    public String getSqlKeyword() {
        return sqlKeyword;
    }
}