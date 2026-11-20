com.hussain.workflow_orchestrator
├── WorkflowOrchestratorApplication.java
├── config/
│   ├── DataApiClientConfig.java              (stub for Stage 1)
│   ├── DefinitionApiProperties.java          (@ConfigurationProperties "definition.api")
│   └── DefaultEntitiesProperties.java        (@ConfigurationProperties "create.workflow.default")
├── client/
│   └── DefinitionApiClient.java              (RestClient → create-and-register)
├── schema/
│   ├── SchemaDefinitionFactory.java
│   ├── SchemaInitializer.java                (ApplicationRunner, fail-fast, flag-gated)
│   └── dto/
│       ├── CreateTableRequest.java
│       ├── ColumnDefinition.java
│       ├── TableConstraints.java
│       ├── PrimaryKeyConstraint.java
│       └── ForeignKeyConstraint.java
├── domain/
│   ├── WorkflowConfig.java                   (record)
│   ├── WorkflowAudit.java                    (record)
│   └── enums/
│       ├── AuditStatus.java                  (SUCCESS, FAILED, TIMED_OUT)
│       └── TriggerType.java                  (MANUAL, SCHEDULED, API, EVENT, RETRY)
└── web/
└── SchemaAdminController.java            (POST /api/v1/workflows/schema/init)