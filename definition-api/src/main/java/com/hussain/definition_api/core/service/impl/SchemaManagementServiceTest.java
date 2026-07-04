//package com.hussain.definition_api.core.service.impl;
//
//import com.hussain.definition_api.core.model.ColumnDefinition;
//import com.hussain.definition_api.core.model.SchemaDefinition;
//import com.hussain.definition_api.core.model.SchemaCreationResult;
//import com.hussain.definition_api.core.enums.ColumnType;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@Testcontainers
//@ActiveProfiles("test")
//public class SchemaManagementServiceTest {
//
//    @Container
//    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
//            .withDatabaseName("test_db")
//            .withUsername("test")
//            .withPassword("test");
//
//    @Autowired
//    private SchemaManagementServiceImpl schemaManagementService;
//
//    @DynamicPropertySource
//    static void properties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgres::getJdbcUrl);
//        registry.add("spring.datasource.username", postgres::getUsername);
//        registry.add("spring.datasource.password", postgres::getPassword);
//    }
//
//    @Test
//    void shouldCreateTableSuccessfully() {
//        SchemaDefinition schemaDef = SchemaDefinition.builder()
//                .schema("public")
//                .tableName("test_table")
//                .column(ColumnDefinition.builder()
//                        .name("id")
//                        .type(ColumnType.BIGINT)
//                        .nullable(false)
//                        .build())
//                .column(ColumnDefinition.builder()
//                        .name("name")
//                        .type(ColumnType.VARCHAR)
//                        .length(100)
//                        .nullable(false)
//                        .build())
//                .build();
//
//        SchemaCreationResult result = schemaManagementService.createTable(schemaDef);
//
//        assertThat(result.isSuccess()).isTrue();
//        assertThat(result.getTableName()).isEqualTo("test_table");
//    }
//}