package com.hussain.data_api.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.jooq.JSON;
import org.jooq.JSONB;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Registers Jackson serializers for jOOQ's JSON types so that SELECTs
 * returning JSONB / JSON columns can be serialized into HTTP responses
 * without needing a per-DTO annotation.
 *
 * <p>Without this, reading any JSONB column causes:
 * <pre>
 *   No serializer found for class org.jooq.JSONB ...
 * </pre>
 * and the response fails with HTTP 500.</p>
 */
@Configuration
public class JacksonJooqConfig {

    @Bean
    public SimpleModule jooqJsonModule() {
        SimpleModule module = new SimpleModule("JooqJsonModule");

        module.addSerializer(JSONB.class, new JsonSerializer<JSONB>() {
            @Override
            public void serialize(JSONB value,
                                  JsonGenerator gen,
                                  SerializerProvider serializers) throws IOException {
                if (value == null) {
                    gen.writeNull();
                } else {
                    // Write the JSON text as raw JSON, not as a quoted string
                    gen.writeRawValue(value.data());
                }
            }
        });

        module.addSerializer(JSON.class, new JsonSerializer<JSON>() {
            @Override
            public void serialize(JSON value,
                                  JsonGenerator gen,
                                  SerializerProvider serializers) throws IOException {
                if (value == null) {
                    gen.writeNull();
                } else {
                    gen.writeRawValue(value.data());
                }
            }
        });

        return module;
    }
}