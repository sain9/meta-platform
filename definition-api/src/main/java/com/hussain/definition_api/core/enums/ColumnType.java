package com.hussain.definition_api.core.enums;

import lombok.Getter;

@Getter
public enum ColumnType {
    // Numeric Types
    SMALLINT("SMALLINT"),
    INTEGER("INTEGER"),
    BIGINT("BIGINT"),
    DECIMAL("DECIMAL"),
    NUMERIC("NUMERIC"),
    REAL("REAL"),
    DOUBLE_PRECISION("DOUBLE PRECISION"),

    // Character Types
    VARCHAR("VARCHAR"),
    CHAR("CHAR"),
    TEXT("TEXT"),

    // Date/Time Types
    DATE("DATE"),
    TIME("TIME"),
    TIMESTAMP("TIMESTAMP"),
    TIMESTAMPTZ("TIMESTAMPTZ"),
    INTERVAL("INTERVAL"),

    // Boolean Type
    BOOLEAN("BOOLEAN"),

    // Binary Types
    BYTEA("BYTEA"),

    // JSON Types
    JSON("JSON"),
    JSONB("JSONB"),

    // UUID Type
    UUID("UUID"),

    // Array Types
    INTEGER_ARRAY("INTEGER[]"),
    VARCHAR_ARRAY("VARCHAR[]"),
    TEXT_ARRAY("TEXT[]"),
    BIGINT_ARRAY("BIGINT[]"),

    // Other Types
    SERIAL("SERIAL"),
    BIGSERIAL("BIGSERIAL"),
    INET("INET"),
    CIDR("CIDR"),
    MACADDR("MACADDR");

    private final String sqlType;

    ColumnType(String sqlType) {
        this.sqlType = sqlType;
    }
}