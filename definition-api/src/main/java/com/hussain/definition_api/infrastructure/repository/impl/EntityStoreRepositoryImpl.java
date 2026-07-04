package com.hussain.definition_api.infrastructure.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hussain.definition_api.core.model.EntityStore;
import com.hussain.definition_api.infrastructure.repository.EntityStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EntityStoreRepositoryImpl implements EntityStoreRepository {

    private final DSLContext dslContext;
    private final ObjectMapper objectMapper;

    private static final String TABLE_NAME = "entity_store";
    private static final String SCHEMA_NAME = "public";

    @Override
    public void createEntityStoreTable() {
        log.info("Creating entity_store table if not exists...");

        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS public.entity_store (
                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                schema_name VARCHAR(63) NOT NULL,
                table_name VARCHAR(63) NOT NULL,
                entity_definition JSONB NOT NULL,
                is_enabled BOOLEAN DEFAULT true,
                version VARCHAR(50),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT uk_entity_store_schema_table UNIQUE (schema_name, table_name)
            )
            """;

        try {
            dslContext.execute(createTableSQL);
            log.info("entity_store table created/verified successfully");

            // Create index for better performance
            String createIndexSQL = """
                CREATE INDEX IF NOT EXISTS idx_entity_store_enabled 
                ON public.entity_store (is_enabled)
                """;
            dslContext.execute(createIndexSQL);

        } catch (Exception e) {
            log.error("Failed to create entity_store table: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create entity_store table", e);
        }
    }

    @Override
    public boolean tableExists(String schema, String tableName) {
        String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = ? AND table_name = ?";

        Integer count = dslContext.fetchOne(sql, schema, tableName)
                .into(Integer.class);

        return count != null && count > 0;
    }

    @Override
    public void save(EntityStore entityStore) {
        log.info("Saving entity definition to entity_store: {}.{}",
                entityStore.getSchemaName(), entityStore.getTableName());

        String sql = """
            INSERT INTO public.entity_store 
            (id, schema_name, table_name, entity_definition, is_enabled, version, created_at, updated_at)
            VALUES (?, ?, ?, ?::jsonb, ?, ?, ?, ?)
            ON CONFLICT (schema_name, table_name) 
            DO UPDATE SET 
                entity_definition = EXCLUDED.entity_definition,
                is_enabled = EXCLUDED.is_enabled,
                version = EXCLUDED.version,
                updated_at = EXCLUDED.updated_at
            """;

        try {
            dslContext.execute(sql,
                    entityStore.getId(),
                    entityStore.getSchemaName(),
                    entityStore.getTableName(),
                    entityStore.getEntityDefinition(),
                    entityStore.isEnabled(),
                    entityStore.getVersion(),
                    entityStore.getCreatedAt() != null ? entityStore.getCreatedAt() : LocalDateTime.now(),
                    LocalDateTime.now()
            );
            log.info("Entity definition saved successfully: {}.{}",
                    entityStore.getSchemaName(), entityStore.getTableName());
        } catch (Exception e) {
            log.error("Failed to save entity definition: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save entity definition", e);
        }
    }

    @Override
    public Optional<EntityStore> findBySchemaAndTable(String schema, String tableName) {
        String sql = """
            SELECT id, schema_name, table_name, entity_definition, is_enabled, 
                   version, created_at, updated_at
            FROM public.entity_store
            WHERE schema_name = ? AND table_name = ?
            """;

        try {
            Record record = dslContext.fetchOne(sql, schema, tableName);
            if (record == null) {
                return Optional.empty();
            }
            return Optional.of(mapRecordToEntityStore(record));
        } catch (Exception e) {
            log.error("Failed to find entity definition: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<EntityStore> findAllEnabled() {
        String sql = """
            SELECT id, schema_name, table_name, entity_definition, is_enabled, 
                   version, created_at, updated_at
            FROM public.entity_store
            WHERE is_enabled = true
            ORDER BY schema_name, table_name
            """;

        try {
            Result<Record> records = dslContext.fetch(sql);
            List<EntityStore> entities = new ArrayList<>();
            for (Record record : records) {
                entities.add(mapRecordToEntityStore(record));
            }
            log.info("Found {} enabled entity definitions", entities.size());
            return entities;
        } catch (Exception e) {
            log.error("Failed to fetch enabled entities: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<EntityStore> findAll() {
        String sql = """
            SELECT id, schema_name, table_name, entity_definition, is_enabled, 
                   version, created_at, updated_at
            FROM public.entity_store
            ORDER BY schema_name, table_name
            """;

        try {
            Result<Record> records = dslContext.fetch(sql);
            List<EntityStore> entities = new ArrayList<>();
            for (Record record : records) {
                entities.add(mapRecordToEntityStore(record));
            }
            return entities;
        } catch (Exception e) {
            log.error("Failed to fetch all entities: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public void updateEnabledStatus(UUID id, boolean enabled) {
        String sql = """
            UPDATE public.entity_store 
            SET is_enabled = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

        try {
            dslContext.execute(sql, enabled, id);
            log.info("Updated enabled status for entity id: {} to {}", id, enabled);
        } catch (Exception e) {
            log.error("Failed to update enabled status: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update enabled status", e);
        }
    }

    @Override
    public void updateEntityDefinition(UUID id, String entityDefinition) {
        String sql = """
            UPDATE public.entity_store 
            SET entity_definition = ?::jsonb, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

        try {
            dslContext.execute(sql, entityDefinition, id);
            log.info("Updated entity definition for id: {}", id);
        } catch (Exception e) {
            log.error("Failed to update entity definition: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update entity definition", e);
        }
    }

    @Override
    public boolean existsBySchemaAndTable(String schema, String tableName) {
        String sql = """
            SELECT COUNT(*) FROM public.entity_store
            WHERE schema_name = ? AND table_name = ?
            """;

        Integer count = dslContext.fetchOne(sql, schema, tableName)
                .into(Integer.class);

        return count != null && count > 0;
    }

    @Override
    public void deleteBySchemaAndTable(String schema, String tableName) {
        String sql = """
            DELETE FROM public.entity_store
            WHERE schema_name = ? AND table_name = ?
            """;

        try {
            dslContext.execute(sql, schema, tableName);
            log.info("Deleted entity definition: {}.{}", schema, tableName);
        } catch (Exception e) {
            log.error("Failed to delete entity definition: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete entity definition", e);
        }
    }

    private EntityStore mapRecordToEntityStore(Record record) {
        return EntityStore.builder()
                .id(record.get("id", UUID.class))
                .schemaName(record.get("schema_name", String.class))
                .tableName(record.get("table_name", String.class))
                .entityDefinition(record.get("entity_definition", String.class))
                .isEnabled(record.get("is_enabled", Boolean.class))
                .version(record.get("version", String.class))
                .createdAt(record.get("created_at", LocalDateTime.class))
                .updatedAt(record.get("updated_at", LocalDateTime.class))
                .build();
    }
}