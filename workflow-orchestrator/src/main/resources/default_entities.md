## Create the workflow_config table
curl -X POST "http://localhost:9996/api/v1/schema/create-and-register" \
-H "Content-Type: application/json" \
-d '{
"schema": "public",
"tableName": "workflow_config",
"ifNotExists": true,
"tableComment": "Stores workflow definitions, source and sink configurations, Data API queries, and execution settings.",
"columns": [
{
"name": "id",
"type": "UUID",
"nullable": false,
"defaultValue": "GEN_RANDOM_UUID()"
},
{
"name": "workflow_name",
"type": "VARCHAR",
"length": 255,
"nullable": false,
"unique": true
},
{
"name": "data_query",
"type": "JSONB",
"nullable": true
},
{
"name": "source_sink_config",
"type": "JSONB",
"nullable": false
},
{
"name": "execution_config",
"type": "JSONB",
"nullable": false
},
{
"name": "enabled",
"type": "BOOLEAN",
"nullable": false,
"defaultValue": "TRUE"
},
{
"name": "created_by",
"type": "VARCHAR",
"length": 255,
"nullable": false
},
{
"name": "created_at",
"type": "TIMESTAMPTZ",
"nullable": false,
"defaultValue": "CURRENT_TIMESTAMP"
},
{
"name": "updated_at",
"type": "TIMESTAMPTZ",
"nullable": false,
"defaultValue": "CURRENT_TIMESTAMP"
},
{
"name": "description",
"type": "TEXT",
"nullable": true
}
],
"constraints": {
"primaryKeys": [
{
"name": "pk_workflow_config",
"columns": ["id"]
}
]
}
}'

## Create the workflow_audit table
curl -X POST "http://localhost:9996/api/v1/schema/create-and-register" \
-H "Content-Type: application/json" \
-d '{
"schema": "public",
"tableName": "workflow_audit",
"ifNotExists": true,
"tableComment": "Stores workflow execution history, execution metrics, runtime details, and errors.",
"columns": [
{
"name": "id",
"type": "UUID",
"nullable": false,
"defaultValue": "GEN_RANDOM_UUID()"
},
{
"name": "workflow_id",
"type": "UUID",
"nullable": false
},
{
"name": "status",
"type": "VARCHAR",
"length": 20,
"nullable": false
},
{
"name": "trigger_type",
"type": "VARCHAR",
"length": 20,
"nullable": false
},
{
"name": "started_at",
"type": "TIMESTAMPTZ",
"nullable": true
},
{
"name": "completed_at",
"type": "TIMESTAMPTZ",
"nullable": true
},
{
"name": "execution_metrics",
"type": "JSONB",
"nullable": true
},
{
"name": "execution_details",
"type": "JSONB",
"nullable": true
},
{
"name": "error_message",
"type": "TEXT",
"nullable": true
},
{
"name": "created_at",
"type": "TIMESTAMPTZ",
"nullable": false,
"defaultValue": "CURRENT_TIMESTAMP"
}
],
"constraints": {
"primaryKeys": [
{
"name": "pk_workflow_audit",
"columns": ["id"]
}
],
"foreignKeys": [
{
"name": "fk_workflow_audit_config",
"columns": ["workflow_id"],
"referencedTable": "workflow_config",
"referencedColumns": ["id"],
"onDelete": "RESTRICT",
"onUpdate": "CASCADE"
}
]
}
}'
