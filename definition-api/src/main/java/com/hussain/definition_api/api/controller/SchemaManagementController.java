package com.hussain.definition_api.api.controller;

import com.hussain.definition_api.api.dto.request.SchemaCreationRequest;
import com.hussain.definition_api.api.dto.request.TableOperationRequest;
import com.hussain.definition_api.api.dto.response.ApiResponse;
import com.hussain.definition_api.api.dto.response.SchemaCreationResponse;
import com.hussain.definition_api.api.mapper.SchemaDefinitionMapperSimplified;
import com.hussain.definition_api.core.exception.SchemaManagementException;
import com.hussain.definition_api.core.exception.TableAlreadyExistsException;
import com.hussain.definition_api.core.model.SchemaCreationResult;
import com.hussain.definition_api.core.model.SchemaInfo;
import com.hussain.definition_api.core.service.SchemaManagementService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/schema")
@RequiredArgsConstructor
public class SchemaManagementController {

    private final SchemaManagementService schemaManagementService;
    private final SchemaDefinitionMapperSimplified mapper;

    @PostConstruct
    public void init() {
        log.info("✅ SchemaManagementController initialized successfully!");
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        log.info("✅ Test endpoint called - Controller is working!");
        return ResponseEntity.ok("Controller is working!");
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<SchemaCreationResponse>> createTable(
            @Valid @RequestBody SchemaCreationRequest request) {

        log.info("📝 Received request to create table: {}.{}",
                request.getSchema(), request.getTableName());

        try {
            var schemaDefinition = mapper.toSchemaDefinition(request);
            SchemaCreationResult result = schemaManagementService.createTable(schemaDefinition);
            var response = mapper.toSchemaCreationResponse(result);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Table created successfully"));
        } catch (TableAlreadyExistsException e) {
            log.warn("Table already exists: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Table already exists: " + e.getMessage()));
        } catch (SchemaManagementException e) {
            log.error("Schema management error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Schema management error: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating table: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create table: " + e.getMessage()));
        }
    }

    @PostMapping("/create-with-audit")
    public ResponseEntity<ApiResponse<SchemaCreationResponse>> createTableWithAudit(
            @Valid @RequestBody SchemaCreationRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("Received request to create table with audit: {}.{}",
                request.getSchema(), request.getTableName());

        try {
            var schemaDefinition = mapper.toSchemaDefinition(request);
            String createdBy = userId != null ? userId : "SYSTEM";

            SchemaCreationResult result = schemaManagementService.createTableWithAudit(
                    schemaDefinition, createdBy);

            var response = mapper.toSchemaCreationResponse(result);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Table created successfully with audit"));
        } catch (Exception e) {
            log.error("Error creating table with audit: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create table: " + e.getMessage()));
        }
    }

    @PostMapping("/create-and-register")
    public ResponseEntity<ApiResponse<SchemaCreationResponse>> createAndRegisterTable(
            @Valid @RequestBody SchemaCreationRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("📝 Received request to create and register table: {}.{}",
                request.getSchema(), request.getTableName());

        try {
            var schemaDefinition = mapper.toSchemaDefinition(request);
            String createdBy = userId != null ? userId : "SYSTEM";

            SchemaCreationResult result = schemaManagementService.createAndRegisterTable(
                    schemaDefinition, createdBy);

            var response = mapper.toSchemaCreationResponse(result);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Table created and registered successfully"));
        } catch (Exception e) {
            log.error("Error creating and registering table: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create and register table: " + e.getMessage()));
        }
    }

    @DeleteMapping("/drop")
    public ResponseEntity<ApiResponse<Void>> dropTable(
            @Valid @RequestBody TableOperationRequest request) {

        log.info("Received request to drop table: {}.{}",
                request.getSchema(), request.getTableName());

        try {
            if (request.isCascade()) {
                schemaManagementService.dropTableCascade(request.getSchema(), request.getTableName());
            } else {
                schemaManagementService.dropTable(request.getSchema(), request.getTableName());
            }

            return ResponseEntity.ok(ApiResponse.success(null, "Table dropped successfully"));
        } catch (Exception e) {
            log.error("Error dropping table: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to drop table: " + e.getMessage()));
        }
    }

    @DeleteMapping("/truncate")
    public ResponseEntity<ApiResponse<Void>> truncateTable(
            @Valid @RequestBody TableOperationRequest request) {

        log.info("Received request to truncate table: {}.{}",
                request.getSchema(), request.getTableName());

        try {
            schemaManagementService.truncateTable(request.getSchema(), request.getTableName());
            return ResponseEntity.ok(ApiResponse.success(null, "Table truncated successfully"));
        } catch (Exception e) {
            log.error("Error truncating table: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to truncate table: " + e.getMessage()));
        }
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<SchemaInfo>> getTableInfo(
            @RequestParam String schema,
            @RequestParam String tableName) {

        log.info("Received request to get table info: {}.{}", schema, tableName);

        try {
            SchemaInfo schemaInfo = schemaManagementService.getTableInfo(schema, tableName);
            return ResponseEntity.ok(ApiResponse.success(schemaInfo, "Table info retrieved successfully"));
        } catch (Exception e) {
            log.error("Error getting table info: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get table info: " + e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<SchemaInfo>>> listTables(
            @RequestParam(defaultValue = "public") String schema) {

        log.info("Received request to list tables in schema: {}", schema);

        try {
            List<SchemaInfo> tables = schemaManagementService.listTables(schema);
            return ResponseEntity.ok(ApiResponse.success(tables, "Tables listed successfully"));
        } catch (Exception e) {
            log.error("Error listing tables: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to list tables: " + e.getMessage()));
        }
    }

    @GetMapping("/exists")
    public ResponseEntity<ApiResponse<Boolean>> tableExists(
            @RequestParam String schema,
            @RequestParam String tableName) {

        log.info("Received request to check if table exists: {}.{}", schema, tableName);

        try {
            boolean exists = schemaManagementService.tableExists(schema, tableName);
            return ResponseEntity.ok(ApiResponse.success(exists, "Table existence check completed"));
        } catch (Exception e) {
            log.error("Error checking table existence: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to check table existence: " + e.getMessage()));
        }
    }
}