package com.hussain.workflow_orchestrator.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hussain.data_api_client.model.Row;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Converts Row values (per data-api-client.txt §6) into typed Java objects.
 *
 * <p>Type table:
 * <ul>
 *   <li>uuid          -> String   -> UUID.fromString</li>
 *   <li>timestamp     -> String   -> OffsetDateTime.parse</li>
 *   <li>jsonb         -> String   -> ObjectMapper.readTree</li>
 *   <li>boolean       -> Boolean  -> cast</li>
 *   <li>varchar/text  -> String   -> cast</li>
 *   <li>integer/bigint-> Long     -> cast</li>
 *   <li>SQL NULL      -> null</li>
 * </ul>
 */
@Component
public class RowMapperHelper {

    private static final Logger log = LoggerFactory.getLogger(RowMapperHelper.class);

    private final ObjectMapper objectMapper;

    public RowMapperHelper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public UUID asUuid(Row row, String column) {
        String value = asString(row, column);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid UUID in column '" + column + "': " + value, ex);
        }
    }

    public String asString(Row row, String column) {
        Object value = row.get(column);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public Boolean asBoolean(Row row, String column) {
        Object value = row.get(column);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        return Boolean.parseBoolean(value.toString());
    }

    public Long asLong(Row row, String column) {
        Object value = row.get(column);
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(value.toString());
    }

    public OffsetDateTime asOffsetDateTime(Row row, String column) {
        String value = asString(row, column);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "Invalid timestamp in column '" + column + "': " + value, ex);
        }
    }

    public JsonNode asJsonNode(Row row, String column) {
        String value = asString(row, column);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(value);
        } catch (Exception ex) {
            log.warn("Failed to parse JSON from column '{}': {}", column, ex.getMessage());
            throw new IllegalArgumentException(
                    "Invalid JSON in column '" + column + "'", ex);
        }
    }
}