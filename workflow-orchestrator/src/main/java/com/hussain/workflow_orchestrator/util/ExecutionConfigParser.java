package com.hussain.workflow_orchestrator.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.hussain.workflow_orchestrator.domain.ExecutionConfig;
import com.hussain.workflow_orchestrator.domain.enums.ExecutionType;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

/**
 * Parses workflow_config.execution_config JSONB into a validated {@link ExecutionConfig}.
 *
 * <p>Fails fast on invalid configuration with a clear, actionable message.
 */
@Component
public class ExecutionConfigParser {

    private static final Logger log = LoggerFactory.getLogger(ExecutionConfigParser.class);

    public static final String DEFAULT_ZONE = "UTC";
    public static final long MIN_INTERVAL_MILLIS = 1_000L;

    /**
     * Parse execution_config JSON. Never returns null — returns MANUAL default if input null/missing.
     *
     * @param json the JSONB column value (nullable)
     * @return validated ExecutionConfig
     * @throws IllegalArgumentException if any field is invalid
     */
    public ExecutionConfig parse(JsonNode json) {
        if (json == null || json.isNull() || json.isMissingNode()) {
            return defaultManual();
        }

        ExecutionType type = parseType(json.path("type").asText(null));

        String cron = textOrNull(json, "cron");
        Long fixedDelayMillis = longOrNull(json, "fixedDelayMillis");
        String zone = textOrNull(json, "zone");
        Long timeoutMillis = longOrNull(json, "timeoutMillis");

        // Validate cron / fixedDelayMillis depending on type
        if (type == ExecutionType.SCHEDULED) {
            boolean hasCron = cron != null && !cron.isBlank();
            boolean hasInterval = fixedDelayMillis != null;

            if (hasCron && hasInterval) {
                throw new IllegalArgumentException(
                        "execution_config: SCHEDULED requires exactly one of 'cron' or "
                                + "'fixedDelayMillis' — both are present");
            }
            if (!hasCron && !hasInterval) {
                throw new IllegalArgumentException(
                        "execution_config: SCHEDULED requires exactly one of 'cron' or "
                                + "'fixedDelayMillis' — neither is present");
            }
            if (hasCron) {
                validateCron(cron);
            } else {
                validateInterval(fixedDelayMillis);
            }
        } else {
            // MANUAL / EVENT — schedule fields ignored
            if (cron != null || fixedDelayMillis != null) {
                log.warn(
                        "execution_config: type={} ignores 'cron'/'fixedDelayMillis' fields",
                        type);
            }
        }

        String effectiveZone = (zone == null || zone.isBlank()) ? DEFAULT_ZONE : zone;
        validateZone(effectiveZone);

        if (timeoutMillis != null) {
            validateTimeout(timeoutMillis);
        }

        return new ExecutionConfig(type, cron, fixedDelayMillis, effectiveZone, timeoutMillis);
    }

    // -------- helpers --------

    private ExecutionConfig defaultManual() {
        return new ExecutionConfig(ExecutionType.MANUAL, null, null, DEFAULT_ZONE, null);
    }

    private ExecutionType parseType(String raw) {
        if (raw == null || raw.isBlank()) {
            return ExecutionType.MANUAL;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        try {
            return ExecutionType.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "execution_config: unknown type '" + raw
                            + "' — allowed: MANUAL, SCHEDULED, EVENT");
        }
    }

    private void validateCron(String cron) {
        try {
            CronExpression.parse(cron);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "execution_config: invalid cron expression '" + cron + "' — " + ex.getMessage(),
                    ex);
        }
    }

    private void validateInterval(Long millis) {
        if (millis < MIN_INTERVAL_MILLIS) {
            throw new IllegalArgumentException(
                    "execution_config: fixedDelayMillis must be >= " + MIN_INTERVAL_MILLIS
                            + " ms — got " + millis);
        }
    }

    private void validateZone(String zone) {
        try {
            ZoneId.of(zone);
        } catch (ZoneRulesException ex) {
            throw new IllegalArgumentException(
                    "execution_config: invalid zone '" + zone + "' — " + ex.getMessage(), ex);
        }
    }

    private void validateTimeout(Long millis) {
        if (millis < MIN_INTERVAL_MILLIS) {
            throw new IllegalArgumentException(
                    "execution_config: timeoutMillis must be >= " + MIN_INTERVAL_MILLIS
                            + " ms — got " + millis);
        }
    }

    private String textOrNull(JsonNode json, String field) {
        JsonNode node = json.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        return node.asText();
    }

    private Long longOrNull(JsonNode json, String field) {
        JsonNode node = json.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isNumber()) {
            throw new IllegalArgumentException(
                    "execution_config: '" + field + "' must be numeric — got " + node);
        }
        return node.asLong();
    }
}