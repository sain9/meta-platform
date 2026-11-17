# Definition API - Metadata Platform

This microservice is the central source of truth for all schema definitions across the meta-platform. It manages database schema lifecycle (create, drop, truncate) and provides metadata retrieval.

## Key Features
*   Schema Definition: Supports complex column definitions (e.g., `DECIMAL(10,2)`).
*   Constraints: Manages Primary, Unique, Foreign Key, and Check Constraints via API.
*   Lifecycle Management: Endpoints for `CREATE`, `DROP`, and `TRUNCATE`.
*   Audit: Integrated audit logging via `X-User-Id` header and `audit_log` table.

## Quick Start Examples

### 1. Create Table (Schema-only)
```bash
curl -X POST http://localhost:8080/api/v1/schema/create \
  -H "Content-Type: application/json" \
  -d '{
    "schema": "public",
    "tableName": "users",
    "columns": [
      {"name": "id", "type": "BIGINT", "nullable": false},
      {"name": "name", "type": "VARCHAR", "length": 100}
    ],
    "constraints": {
      "primaryKeys": [{"name": "pk_users", "columns": ["id"]}]
    }
  }'
```

### 2. Create & Register (For Consumers)
Use this for creating a table AND registering it in the `entity_store` for API consumption:

```bash
curl -X POST http://localhost:8080/api/v1/schema/create-and-register \
-H "Content-Type: application/json" \
-H "X-User-Id: admin@company.com" \
-d '{
"schema": "public",
"tableName": "products",
"version": "1.0",
"columns": [
  {"name": "product_id", "type": "BIGINT", "nullable": false, "identity": true},
  {"name": "product_name", "type": "VARCHAR", "length": 200, "nullable": false},
  {"name": "price", "type": "DECIMAL", "precision": 10, "scale": 2, "nullable": false}
],
"constraints": {
"primaryKeys": [{"name": "pk_products", "columns": ["product_id"]}]
}
}'
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/schema/create` | Create a new table (Schema only) |
| `POST` | `/schema/create-and-register` | Create table and register in `entity_store` |
| `DELETE` | `/schema/drop` | Drop a specified table |
| `DELETE` | `/schema/truncate` | Truncate all data from a table |
| `GET` | `/schema/list` | List all available tables in a schema |
| `GET` | `/schema/info` | Get detailed table metadata |
| `GET` | `/schema/exists` | Check table existence |
| `GET` | `/actuator/health` | Health check |

## Schema Definition Structure (JSON)

The input structure allows full PostgreSQL DDL definition:

*   **`schema`**: Target schema (e.g., `"public"`).
*   **`tableName`**: Name of the table.
*   **`columns`**: Array of column definitions (includes `name`, `type`, `length`, `precision`, `scale`, `nullable`, `unique`, `defaultValue`, `identity`, `comment`).
*   **`constraints`**: Object containing arrays for `primaryKeys`, `uniqueKeys`, `foreignKeys`, and `checkConstraints`.

## Data Types Supported

| Category | Type | Example Usage |
|---|---|---|
| **Numeric** | `DECIMAL`/`NUMERIC` | `"type": "DECIMAL", "precision": 10, "scale": 2` |
| **Integers** | `BIGINT`, `BIGSERIAL` | `"type": "BIGINT", "identity": true` |
| **String** | `VARCHAR`, `TEXT` | `"type": "VARCHAR", "length": 255` |
| **Date/Time**| `TIMESTAMP`, `DATE` | `"type": "TIMESTAMP", "defaultValue": "CURRENT_TIMESTAMP"` |
| **Special** | `UUID`, `JSONB` | `"type": "UUID", "defaultValue": "GEN_RANDOM_UUID()"` |

**Note on Defaults:** Supported defaults include `"CURRENT_TIMESTAMP"`, `"NOW()"`, `"CURRENT_DATE"`, `"CURRENT_TIME"`, and UUID generation functions.

## Best Practices

1.  **Naming:** Use lowercase with underscores (e.g., `user_id`, `pk_users`).
2.  **Identity:** Use `"identity": true` for auto-incrementing columns (e.g., `BIGSERIAL`).
3.  **Auditing:** Use `X-User-Id` header for creating tables that require audit tracking.

**Support:** Check the health endpoint at `http://localhost:8080/api/v1/actuator/health` for service status.