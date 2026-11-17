# Data API Documentation

Welcome to the Data API documentation. This guide provides a comprehensive overview of the available operations on the "parts" entity, including CRUD, bulk, batch, and health checks.

## gRPC Testing

### macOS
Install `grpcurl` using Homebrew:

```bash
brew install grpcurl
```

### Linux (with Go)
Install `grpcurl` via Go:

```bash
go install github.com/fullstorydev/grpcurl/cmd/grpcurl@latest
```

### List Available Services
List services to verify reflection works:

```bash
grpcurl -plaintext localhost:9003 list
```

**Expected Output**
```
com.hussain.data_api.grpc.proto.DataApiService
grpc.health.v1.Health
grpc.reflection.v1alpha.ServerReflection
```

### Describe Service
Describe the `DataApiService`:

```bash
grpcurl -plaintext localhost:9003 describe com.hussain.data_api.grpc.proto.DataApiService
```

**Expected Output**
```
rpc Execute
rpc BatchExecute
rpc Info
```

### Call Info
Call the `Info` method without a proto file:

```bash
grpcurl -plaintext -d '{}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Info
```

**Expected Output**
```json
{
  "service": "data-api",
  "version": "1.0.0",
  "status": "UP",
  "uptimeSeconds": "12"
}
```

### Check Health Service
Check the standard gRPC health service:

```bash
grpcurl -plaintext -d '{"service": ""}' localhost:9003 grpc.health.v1.Health/Check
```

**Expected Output**
```json
{
  "status": "SERVING"
}
```

## CRUD Operations

### CREATE

#### Single Record
Create a single record in the `parts` entity:

```bash
grpcurl -plaintext -d '{
    "operation": "CREATE",
    "entity": "part",
    "data": {
        "short_part_number": {"stringValue": "SP-BULK-A"},
        "ecc": {"stringValue": "ECC-A"},
        "name": {"stringValue": "Test Part A"},
        "brand_minor": {"stringValue": "Bosch"},
        "uuid": {"stringValue": "aaaaaaaa-0000-0000-0000-000000000001"},
        "code": {"stringValue": "BULK-A-1"},
        "saleable": {"stringValue": "Y"},
        "segment": {"stringValue": "Braking"}
    }
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
INSERT INTO parts (name, partowner, description, quantity)
VALUES ('Test Part A', 'Bosch', 'Test Part A', 'Y');
```

#### With NULL Values
Create a record with a `NULL` description:

```bash
grpcurl -plaintext -d '{
    "operation": "CREATE",
    "entity": "part",
    "data": {
        "name": "Part With Null Desc",
        "partowner": "Jane Smith",
        "description": null,
        "quantity": 50
    }
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
INSERT INTO parts (name, partowner, description, quantity)
VALUES ('Part With Null Desc', 'Jane Smith', NULL, 50);
```

### READ

#### Select All Columns
Read all columns from the `parts` entity:

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "description", "quantity"]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner, description, quantity FROM parts
```

#### Select Specific Columns
Read specific columns:

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "quantity"]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, quantity FROM parts
```

#### WHERE EQ Filter
Read records where `partowner` equals "John Doe":

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"],
    "filters": [
        {
            "column": "partowner",
            "operator": "EQ",
            "value": "John Doe"
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner, quantity FROM parts WHERE partowner = 'John Doe'
```

#### WHERE NEQ Filter
Read records where `partowner` is not "John Doe":

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"],
    "filters": [
        {
            "column": "partowner",
            "operator": "NEQ",
            "value": "John Doe"
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner, quantity FROM parts WHERE partowner <> 'John Doe'
```

#### WHERE GT Filter
Read records where `quantity` is greater than 50:

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"],
    "filters": [
        {
            "column": "quantity",
            "operator": "GT",
            "value": 50
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner, quantity FROM parts WHERE quantity > 50
```

#### WHERE GTE Filter
Read records where `quantity` is greater than or equal to 100:

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"],
    "filters": [
        {
            "column": "quantity",
            "operator": "GTE",
            "value": 100
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner, quantity FROM parts WHERE quantity >= 100
```

#### WHERE LT Filter
Read records where `quantity` is less than 100:

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"],
    "filters": [
        {
            "column": "quantity",
            "operator": "LT",
            "value": 100
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner, quantity FROM parts WHERE quantity < 100
```

#### WHERE LTE Filter
Read records where `quantity` is less than or equal to 100:

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"],
    "filters": [
        {
            "column": "quantity",
            "operator": "LTE",
            "value": 100
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner, quantity FROM parts WHERE quantity <= 100
```

#### WHERE LIKE Filter
Read records where `name` contains "Widget":

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"],
    "filters": [
        {
            "column": "name",
            "operator": "LIKE",
            "value": "Widget"
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner, quantity FROM parts WHERE name LIKE '%Widget%'
```

#### WHERE ILIKE Filter (Case‑insensitive)
Read records where `name` contains "widget" (case‑insensitive):

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"],
    "filters": [
        {
            "column": "name",
            "operator": "ILIKE",
            "value": "widget"
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner, quantity FROM parts WHERE name ILIKE '%widget%'
```

#### WHERE IN Filter
Read records where `partowner` is "John Doe", "Alice Smith", or "Carol White":

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"],
    "filters": [
        {
            "column": "partowner",
            "operator": "IN",
            "value": ["John Doe", "Alice Smith", "Carol White"]
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner, quantity FROM parts WHERE partowner IN ('John Doe', 'Alice Smith', 'Carol White')
```

#### WHERE NOT IN Filter
Read records where `partowner` is not "John Doe" or "Alice Smith":

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"],
    "filters": [
        {
            "column": "partowner",
            "operator": "NOT_IN",
            "value": ["John Doe", "Alice Smith"]
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner, quantity FROM parts WHERE partowner NOT IN ('John Doe', 'Alice Smith')
```

#### WHERE IS NULL Filter
Read records where `description` is `NULL`:

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "description", "quantity"],
    "filters": [
        {
            "column": "description",
            "operator": "IS_NULL"
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner, description, quantity FROM parts WHERE description IS NULL
```

#### WHERE IS NOT NULL Filter
Read records where `description` is not `NULL`:

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "description", "quantity"],
    "filters": [
        {
            "column": "description",
            "operator": "IS_NOT_NULL"
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner, description, quantity FROM parts WHERE description IS NOT NULL
```

#### WHERE BETWEEN Filter
Read records where `quantity` is between 50 and 200:

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"],
    "filters": [
        {
            "column": "quantity",
            "operator": "BETWEEN",
            "value": [50, 200]
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner, quantity FROM parts WHERE quantity BETWEEN 50 AND 200
```

#### WHERE CONTAINS Filter
Read records where `description` contains "quality":

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "description"],
    "filters": [
        {
            "column": "description",
            "operator": "CONTAINS",
            "value": "quality"
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner, description FROM parts WHERE description LIKE '%quality%'
```

#### WHERE STARTS_WITH Filter
Read records where `name` starts with "New":

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner"],
    "filters": [
        {
            "column": "name",
            "operator": "STARTS_WITH",
            "value": "New"
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner FROM parts WHERE name LIKE 'New%'
```

#### WHERE ENDS_WITH Filter
Read records where `name` ends with "Widget":

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner"],
    "filters": [
        {
            "column": "name",
            "operator": "ENDS_WITH",
            "value": "Widget"
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner FROM parts WHERE name LIKE '%Widget'
```

#### Multiple Filters (AND)
Read records where `partowner` is "John Doe" and `quantity` is greater than 50:

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"],
    "filters": [
        {
            "column": "partowner",
            "operator": "EQ",
            "value": "John Doe",
            "logicalOperator": "AND"
        },
        {
            "column": "quantity",
            "operator": "GT",
            "value": 50,
            "logicalOperator": "AND"
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner, quantity FROM parts WHERE partowner = 'John Doe' AND quantity > 50
```

#### Multiple Filters (OR)
Read records where `partowner` is "John Doe" or "Alice Smith":

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"],
    "filters": [
        {
            "column": "partowner",
            "operator": "EQ",
            "value": "John Doe",
            "logicalOperator": "AND"
        },
        {
            "column": "partowner",
            "operator": "EQ",
            "value": "Alice Smith",
            "logicalOperator": "OR"
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner, quantity FROM parts WHERE partowner = 'John Doe' OR partowner = 'Alice Smith'
```

#### ORDER BY ASC
Read records ordered by `name` ascending:

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"],
    "orderBy": [
        {
            "column": "name",
            "direction": "ASC"
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner, quantity FROM parts ORDER BY name ASC
```

#### ORDER BY DESC
Read records ordered by `quantity` descending:

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"],
    "orderBy": [
        {
            "column": "quantity",
            "direction": "DESC"
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner, quantity FROM parts ORDER BY quantity DESC
```

#### Multiple ORDER BY
Read records ordered first by `partowner` ascending, then by `quantity` descending:

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"],
    "orderBy": [
        {
            "column": "partowner",
            "direction": "ASC"
        },
        {
            "column": "quantity",
            "direction": "DESC"
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner, quantity FROM parts ORDER BY partowner ASC, quantity DESC
```

#### LIMIT Only
Read up to 5 records:

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"],
    "limit": 5
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner, quantity FROM parts LIMIT 5
```

#### LIMIT and OFFSET (Pagination)
Read records 6‑10 (page 2, size 5):

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"],
    "limit": 5,
    "offset": 10
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT name, partowner, quantity FROM parts LIMIT 5 OFFSET 10
```

#### GROUP BY
Read count of parts per `partowner`:

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["partowner", "COUNT(name) as total_parts"],
    "groupBy": [
        {
            "column": "partowner"
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT partowner, COUNT(name) as total_parts FROM parts GROUP BY partowner
```

#### GROUP BY and HAVING
Read part owners with total quantity greater than 100:

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["partowner", "COUNT(name) as total_parts", "SUM(quantity) as total_quantity"],
    "groupBy": [
        {
            "column": "partowner"
        }
    ],
    "having": [
        {
            "column": "quantity",
            "aggregate": "SUM",
            "operator": "GT",
            "value": 100
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT partowner, COUNT(name) as total_parts, SUM(quantity) as total_quantity
FROM parts
GROUP BY partowner
HAVING SUM(quantity) > 100
```

#### YEAR GROUP BY
Read number of parts created each year:

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["YEAR(created_at) as year", "COUNT(name) as total_parts"],
    "groupBy": [
        {
            "column": "created_at",
            "type": "YEAR",
            "alias": "year"
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT EXTRACT(YEAR FROM created_at) as year, COUNT(name) as total_parts
FROM parts
GROUP BY EXTRACT(YEAR FROM created_at)
```

#### DATE_TRUNC GROUP BY
Read count of parts per month:

```bash
grpcurl -plaintext -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["DATE_TRUNC(created_at) as month", "COUNT(name) as total_parts"],
    "groupBy": [
        {
            "column": "created_at",
            "type": "DATE_TRUNC",
            "format": "month",
            "alias": "month"
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
SELECT DATE_TRUNC('month', created_at) as month, COUNT(name) as total_parts
FROM parts
GROUP BY DATE_TRUNC('month', created_at)
```

## UPDATE Operations

### Single Column
Update the `quantity` of a part named "New Widget Part":

```bash
grpcurl -plaintext -d '{
    "operation": "UPDATE",
    "entity": "parts",
    "data": {
        "quantity": 200
    },
    "filters": [
        {
            "column": "name",
            "operator": "EQ",
            "value": "New Widget Part"
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
UPDATE parts SET quantity = 200 WHERE name = 'New Widget Part'
```

### Multiple Columns
Update `quantity`, `description`, and `partowner` for "Premium Widget":

```bash
grpcurl -plaintext -d '{
    "operation": "UPDATE",
    "entity": "parts",
    "data": {
        "quantity": 150,
        "description": "Updated description - Premium quality",
        "partowner": "Jane Smith"
    },
    "filters": [
        {
            "column": "name",
            "operator": "EQ",
            "value": "Premium Widget"
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
UPDATE parts
SET quantity = 150, description = 'Updated description - Premium quality', partowner = 'Jane Smith'
WHERE name = 'Premium Widget'
```

## DELETE Operations

### Single Filter
Delete the part named "Lightweight Nut":

```bash
grpcurl -plaintext -d '{
    "operation": "DELETE",
    "entity": "parts",
    "filters": [
        {
            "column": "name",
            "operator": "EQ",
            "value": "Lightweight Nut"
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
DELETE FROM parts WHERE name = 'Lightweight Nut'
```

### Multiple Filters
Delete parts where `partowner` is "Bob Johnson" and `quantity` is less than 100:

```bash
grpcurl -plaintext -d '{
    "operation": "DELETE",
    "entity": "parts",
    "filters": [
        {
            "column": "partowner",
            "operator": "EQ",
            "value": "Bob Johnson",
            "logicalOperator": "AND"
        },
        {
            "column": "quantity",
            "operator": "LT",
            "value": 100,
            "logicalOperator": "AND"
        }
    ]
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
DELETE FROM parts WHERE partowner = 'Bob Johnson' AND quantity < 100
```

## BULK Operations

### BULK CREATE
Create multiple parts in a single request:

```bash
grpcurl -plaintext -d '{
    "operation": "BULK_CREATE",
    "entity": "parts",
    "data": {
        "items": [
            {
                "name": "Standard Bolt",
                "partowner": "Bob Johnson",
                "description": "Standard grade bolt",
                "quantity": 200
            },
            {
                "name": "Heavy Duty Screw",
                "partowner": "Carol White",
                "description": "Heavy duty screw",
                "quantity": 150
            },
            {
                "name": "Lightweight Nut",
                "partowner": "David Brown",
                "description": "Lightweight nut",
                "quantity": 300
            }
        ]
    }
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
INSERT INTO parts (name, partowner, description, quantity) VALUES ('Standard Bolt', 'Bob Johnson', 'Standard grade bolt', 200);
INSERT INTO parts (name, partowner, description, quantity) VALUES ('Heavy Duty Screw', 'Carol White', 'Heavy duty screw', 150);
INSERT INTO parts (name, partowner, description, quantity) VALUES ('Lightweight Nut', 'David Brown', 'Lightweight nut', 300);
```

### BULK UPDATE
Update multiple parts in a single request:

```bash
grpcurl -plaintext -d '{
    "operation": "BULK_UPDATE",
    "entity": "parts",
    "data": {
        "items": [
            {
                "name": "New Widget Part",
                "quantity": 250,
                "description": "Bulk updated"
            },
            {
                "name": "Premium Widget",
                "quantity": 350,
                "description": "Bulk updated - Premium"
            }
        ]
    }
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
UPDATE parts SET quantity = 250, description = 'Bulk updated' WHERE name = 'New Widget Part';
UPDATE parts SET quantity = 350, description = 'Bulk updated - Premium' WHERE name = 'Premium Widget';
```

### BULK DELETE
Delete multiple parts by ID:

```bash
grpcurl -plaintext -d '{
    "operation": "BULK_DELETE",
    "entity": "parts",
    "data": {
        "ids": [1, 2, 3]
    }
}' localhost:9003 com.hussain.data_api.grpc.proto.DataApiService/Execute
```

**Generated SQL**
```sql
DELETE FROM parts WHERE id IN (1, 2, 3)
```

## BATCH Operations

### BATCH - Multiple Operations
Perform multiple operations in a single batch request:

```bash
curl -X POST http://localhost:8081/api/data/batch \
  -H "Content-Type: application/json" \
  -d '[
    {
      "operation": "READ",
      "entity": "parts",
      "columns": ["name", "partowner"],
      "limit": 5
    },
    {
      "operation": "CREATE",
      "entity": "parts",
      "data": {
        "name": "Batch Created Part",
        "partowner": "System User",
        "description": "Created via batch",
        "quantity": 25
      }
    },
    {
      "operation": "READ",
      "entity": "parts",
      "columns": ["name", "partowner", "quantity"],
      "filters": [
        {
            "column": "partowner",
            "operator": "EQ",
            "value": "System User"
        }
      ]
    }
  ]'
```

**Generated SQL**
```sql
-- Operation 1:
SELECT name, partowner FROM parts LIMIT 5

-- Operation 2:
INSERT INTO parts (name, partowner, description, quantity)
VALUES ('Batch Created Part', 'System User', 'Created via batch', 25)

-- Operation 3:
SELECT name, partowner, quantity FROM parts WHERE partowner = 'System User'
```

## Health Check

Check the health of the Data API:

```bash
curl -X GET http://localhost:8081/api/data/health
```

**Response**
```json
{
  "status": "UP",
  "service": "data-api"
}
```

## Complete Table Schema

```sql
CREATE TABLE parts (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  partowner VARCHAR(255) NOT NULL,
  description TEXT,
  quantity INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Sample Data for Testing

```sql
INSERT INTO parts (name, partowner, description, quantity) VALUES
('New Widget Part', 'John Doe', 'A high-quality widget part', 100),
('Premium Widget', 'Alice Smith', 'Premium quality widget', 50),
('Standard Bolt', 'Bob Johnson', 'Standard grade bolt', 200),
('Heavy Duty Screw', 'Carol White', 'Heavy duty screw for industrial use', 150),
('Lightweight Nut', 'David Brown', 'Lightweight nut for precision work', 300),
('Super Widget', 'John Doe', 'Super quality widget', 75),
('Mini Widget', 'Alice Smith', 'Mini widget for small applications', 25);
```

## JOINS, SUBQUERY, Nested Subquery

### Example 1

Complex JOIN with subqueries and filters:

```bash
curl -X POST http://localhost:9995/api/data/execute \
  -H "Content-Type: application/json" \
  -d '{
    "operation": "READ",
    "from": { "entity": "public.part", "alias": "A0" },
    "columns": [
      "A0.short_part_number", "A0.ecc", "A0.name",
      "A0.brand_minor", "A0.uuid", "A1.part_number"
    ],
    "joins": [{
      "type": "LEFT",
      "alias": "A1",
      "subquery": {
        "query": {
          "operation": "READ",
          "from": { "entity": "public.dn_pricing" },
          "columns": ["part_number"],
          "filterGroup": {
            "logicalOperator": "OR",
            "groups": [{
              "logicalOperator": "AND",
              "filters": [
                { "column": "uuid",   "operator": "IS_NOT_NULL" },
                { "column": "region", "operator": "NOT_IN", "value": [20, 23, 6] }
              ]
            }],
            "filters": [{ "column": "region", "operator": "IS_NULL" }]
          }
        }
      },
      "on": [{ "left": "A1.part_number", "operator": "EQ", "right": "A0.code" }]
    }],
    "filters": [
      { "column": "A0.short_part_number", "operator": "IS_NOT_NULL", "logicalOperator": "AND" },
      { "column": "A0.ecc",               "operator": "IS_NOT_NULL", "logicalOperator": "AND" },
      { "column": "A0.saleable",          "operator": "IS_NOT_NULL", "logicalOperator": "AND" },
      { "column": "A0.saleable",          "operator": "NEQ",         "value": "N", "logicalOperator": "AND" },
      { "column": "A0.segment",           "operator": "IS_NULL",     "logicalOperator": "AND" },
      { "column": "A1.part_number",       "operator": "IS_NULL",     "logicalOperator": "AND" }
    ]
  }'
```

**Expected SQL**
```sql
SELECT A0.short_part_number, A0.ecc, A0.name, A0.brand_minor, A0.uuid, A1.part_number
FROM public.part A0
LEFT JOIN (
  SELECT part_number FROM public.dn_pricing
  WHERE (uuid IS NOT NULL AND region NOT IN (20, 23, 6)) OR region IS NULL
) A1 ON A1.part_number = A0.code
WHERE A0.short_part_number IS NOT NULL
  AND A0.ecc IS NOT NULL
  AND A0.saleable IS NOT NULL
  AND A0.saleable <> 'N'
  AND A0.segment IS NULL
  AND A1.part_number IS NULL
```

### Example 2

Simple LEFT JOIN:

```bash
curl -X POST http://localhost:9995/api/data/execute \
  -H "Content-Type: application/json" \
  -d '{
    "operation": "READ",
    "from": { "entity": "parts", "alias": "p" },
    "joins": [{
      "type": "LEFT",
      "entity": "suppliers",
      "alias": "s",
      "on": [{ "left": "s.part_name", "operator": "EQ", "right": "p.name" }]
    }],
    "columns": ["p.name"],
    "filters": [
      { "column": "s.part_name", "operator": "IS_NULL" }
    ]
}' 
```

**Expected SQL**
```sql
SELECT p.name
FROM parts p
LEFT JOIN suppliers s ON s.part_name = p.name
WHERE s.part_name IS NULL
```

### Example 3

Nested subquery:

```bash
curl -X POST http://localhost:9995/api/data/execute \
  -H "Content-Type: application/json" \
  -d '{
    "operation": "READ",
    "from": { "entity": "parts", "alias": "p" },
    "columns": ["p.name"],
    "filters": [
        {
            "column": "p.name",
            "operator": "IN",
            "subquery": {
                "query": {
                    "operation": "READ",
                    "from": { "entity": "suppliers", "alias": "s" },
                    "columns": ["s.part_name"],
                    "filters": [
                        {
                            "column": "s.part_name",
                            "operator": "IN",
                            "subquery": {
                                "query": {
                                    "operation": "READ",
                                    "from": { "entity": "orders", "alias": "o" },
                                    "columns": ["o.part_name"],
                                    "filters": [
                                        {
                                            "column": "o.part_name",
                                            "operator": "IN",
                                            "subquery": {
                                                "query": {
                                                    "operation": "READ",
                                                    "from": { "entity": "inventory" },
                                                    "columns": ["part_name"],
                                                    "filters": [{ "column": "stock", "operator": "GT", "value": 100 }]
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    ]
                }
            }
        }
    ]
  }'
```

**Expected SQL**
```sql
SELECT p.name
FROM parts p
WHERE p.name IN (
  SELECT s.part_name FROM suppliers s
  WHERE s.part_name IN (
    SELECT o.part_name FROM orders o
    WHERE o.part_name IN (
      SELECT part_name FROM inventory WHERE stock > 100
    )
  )
)
```

**Footer**  
© 2026 Data API Documentation. All rights reserved.