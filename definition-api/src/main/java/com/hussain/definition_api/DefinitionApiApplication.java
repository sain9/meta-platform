package com.hussain.definition_api;

import com.hussain.definition_api.core.service.SchemaManagementService;
import com.hussain.definition_api.infrastructure.repository.EntityStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@EnableAsync
@SpringBootApplication
@ComponentScan(basePackages = {"com.hussain.definition_api"})
@RequiredArgsConstructor
public class DefinitionApiApplication implements CommandLineRunner {

    private final EntityStoreRepository entityStoreRepository;
    private final SchemaManagementService schemaManagementService;

    public static void main(String[] args) {
        var context = SpringApplication.run(DefinitionApiApplication.class, args);

        int port = context.getEnvironment().getProperty("server.port", Integer.class, 8080);

        log.info("============================================================");
        log.info("Definition API Application started successfully!");
        log.info("API Base Path: /api/v1");
        log.info("Health Check: http://localhost:{}/api/v1/actuator/health", port);
        log.info("============================================================");
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Running startup tasks...");

        // 1. Create entity_store table if not exists
        entityStoreRepository.createEntityStoreTable();
        log.info("✅ entity_store table verified/created");

        // 2. Sync enabled entities - create missing tables
        log.info("🔄 Syncing enabled entities from entity_store...");
        schemaManagementService.syncEnabledEntities();
        log.info("✅ Sync completed");
    }
}