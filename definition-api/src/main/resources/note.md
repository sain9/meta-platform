definition-api/
├── pom.xml
├── README.md
├── docker-compose.yml
├── Dockerfile
├── .gitignore
│
├── docs/
│   ├── API_GUIDE.md
│   └── index.html
│
└── src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── hussain/
│   │           └── definition_api/
│   │               ├── DefinitionApiApplication.java
│   │               │
│   │               ├── api/
│   │               │   ├── controller/
│   │               │   │   ├── SchemaManagementController.java
│   │               │   │   └── SimpleTestController.java
│   │               │   │
│   │               │   ├── dto/
│   │               │   │   ├── request/
│   │               │   │   │   ├── SchemaCreationRequest.java
│   │               │   │   │   └── TableOperationRequest.java
│   │               │   │   └── response/
│   │               │   │       ├── ApiResponse.java
│   │               │   │       ├── ErrorResponse.java
│   │               │   │       └── SchemaCreationResponse.java
│   │               │   │
│   │               │   ├── handler/
│   │               │   │   └── GlobalExceptionHandler.java
│   │               │   │
│   │               │   └── mapper/
│   │               │       └── SchemaDefinitionMapperSimplified.java
│   │               │
│   │               ├── core/
│   │               │   ├── enums/
│   │               │   │   ├── ColumnType.java
│   │               │   │   ├── ConstraintType.java
│   │               │   │   └── ReferentialAction.java
│   │               │   │
│   │               │   ├── exception/
│   │               │   │   ├── ConstraintViolationException.java
│   │               │   │   ├── InvalidSchemaDefinitionException.java
│   │               │   │   ├── SchemaManagementException.java
│   │               │   │   └── TableAlreadyExistsException.java
│   │               │   │
│   │               │   ├── model/
│   │               │   │   ├── CheckConstraint.java
│   │               │   │   ├── ColumnDefinition.java
│   │               │   │   ├── Constraint.java
│   │               │   │   ├── Constraints.java
│   │               │   │   ├── ForeignKey.java
│   │               │   │   ├── PrimaryKey.java
│   │               │   │   ├── SchemaCreationResult.java
│   │               │   │   ├── SchemaDefinition.java
│   │               │   │   ├── SchemaInfo.java
│   │               │   │   ├── TableMetadata.java
│   │               │   │   └── UniqueKey.java
│   │               │   │
│   │               │   └── service/
│   │               │       ├── impl/
│   │               │       │   ├── SchemaManagementServiceImpl.java
│   │               │       │   ├── SchemaManagementServiceTest.java
│   │               │       │   ├── SchemaMetadataServiceImpl.java
│   │               │       │   └── SchemaValidationServiceImpl.java
│   │               │       ├── SchemaManagementService.java
│   │               │       ├── SchemaMetadataService.java
│   │               │       └── SchemaValidationService.java
│   │               │
│   │               ├── infrastructure/
│   │               │   ├── audit/
│   │               │   │   ├── AuditLogger.java
│   │               │   │   └── SchemaAuditEvent.java
│   │               │   │
│   │               │   ├── config/
│   │               │   │   ├── DataSourceConfig.java
│   │               │   │   └── JooqConfig.java
│   │               │   │
│   │               │   └── repository/
│   │               │       ├── impl/
│   │               │       │   └── SchemaRepositoryImpl.java
│   │               │       └── SchemaRepository.java
│   │               │
│   │               └── shared/
│   │                   └── logging/
│   │                       ├── LoggingAspect.java
│   │                       └── PerformanceLogger.java
│   │
│   └── resources/
│       ├── application.properties
│       ├── application.yml
│       ├── logback-spring.xml
│       └── db/
│           └── migration/
│               └── V1__create_audit_tables.sql
│
└── test/
└── java/
└── com/
└── hussain/
└── definition_api/
└── core/
└── service/
└── impl/
└── SchemaManagementServiceTest.java