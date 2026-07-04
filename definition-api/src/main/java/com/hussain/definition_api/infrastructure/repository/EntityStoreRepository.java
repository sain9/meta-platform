package com.hussain.definition_api.infrastructure.repository;

import com.hussain.definition_api.core.model.EntityStore;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EntityStoreRepository {

    void createEntityStoreTable();

    boolean tableExists(String schema, String tableName);

    void save(EntityStore entityStore);

    Optional<EntityStore> findBySchemaAndTable(String schema, String tableName);

    List<EntityStore> findAllEnabled();

    List<EntityStore> findAll();

    void updateEnabledStatus(UUID id, boolean enabled);

    void updateEntityDefinition(UUID id, String entityDefinition);

    boolean existsBySchemaAndTable(String schema, String tableName);

    void deleteBySchemaAndTable(String schema, String tableName);
}