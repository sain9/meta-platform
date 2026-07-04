package com.hussain.definition_api.api.controller;

import com.hussain.definition_api.api.dto.response.ApiResponse;
import com.hussain.definition_api.core.model.EntityStore;
import com.hussain.definition_api.infrastructure.repository.EntityStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/entity-store")
@RequiredArgsConstructor
public class EntityStoreController {

    private final EntityStoreRepository entityStoreRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EntityStore>>> getAllEntities() {
        List<EntityStore> entities = entityStoreRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(entities, "Entities retrieved successfully"));
    }

    @GetMapping("/enabled")
    public ResponseEntity<ApiResponse<List<EntityStore>>> getEnabledEntities() {
        List<EntityStore> entities = entityStoreRepository.findAllEnabled();
        return ResponseEntity.ok(ApiResponse.success(entities, "Enabled entities retrieved successfully"));
    }

    @GetMapping("/{schema}/{tableName}")
    public ResponseEntity<ApiResponse<EntityStore>> getEntity(
            @PathVariable String schema,
            @PathVariable String tableName) {
        
        var entity = entityStoreRepository.findBySchemaAndTable(schema, tableName);
        return entity.map(e -> ResponseEntity.ok(ApiResponse.success(e, "Entity retrieved successfully")))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleEntity(
            @PathVariable UUID id,
            @RequestParam boolean enabled) {
        
        entityStoreRepository.updateEnabledStatus(id, enabled);
        return ResponseEntity.ok(ApiResponse.success(null, "Entity status updated successfully"));
    }

    @DeleteMapping("/{schema}/{tableName}")
    public ResponseEntity<ApiResponse<Void>> deleteEntity(
            @PathVariable String schema,
            @PathVariable String tableName) {
        
        entityStoreRepository.deleteBySchemaAndTable(schema, tableName);
        return ResponseEntity.ok(ApiResponse.success(null, "Entity deleted successfully"));
    }
}