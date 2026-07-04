if you want to create table + register in entity_store then follow the following curl structure
curl -X POST http://localhost:8080/api/v1/schema/create-and-register \
-H "Content-Type: application/json" \
-H "X-User-Id: admin@company.com" \
-d '{
"schema": "public",
"tableName": "products",
"version": "1.0",
"columns": [
{
"name": "product_id",
"type": "BIGINT",
"nullable": false,
"identity": true
},
{
"name": "product_name",
"type": "VARCHAR",
"length": 200,
"nullable": false
},
{
"name": "price",
"type": "DECIMAL",
"precision": 10,
"scale": 2,
"nullable": false
}
],
"constraints": {
"primaryKeys": [
{
"name": "pk_products",
"columns": ["product_id"]
}
]
}
}'

the following is just create without registering structures but it is valid for dataypes contraint and
everything else


Create Your First Table
bash
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

# API Endpoints
Method	Endpoint	Description
POST	/schema/create	Create a new table
POST	/schema/create-with-audit	Create table with audit logging
DELETE	/schema/drop	Drop a table
DELETE	/schema/truncate	Truncate a table
GET	/schema/list	List all tables
GET	/schema/info	Get table information
GET	/schema/exists	Check if table exists
GET	/actuator/health	Health check


# Schema Definition Structure
{
"schema": "public",
"tableName": "table_name",
"ifNotExists": false,
"temporary": false,
"tableComment": "Table description",
"columns": [
{
"name": "column_name",
"type": "DATA_TYPE",
"length": 255,
"precision": 10,
"scale": 2,
"nullable": false,
"unique": false,
"defaultValue": "default_value",
"identity": false,
"comment": "Column description"
}
],
"constraints": {
"primaryKeys": [...],
"uniqueKeys": [...],
"foreignKeys": [...],
"checkConstraints": [...]
}
}


Data Types
Numeric Types
Type	Description	Usage
SMALLINT	Small integer	"type": "SMALLINT"
INTEGER	Integer	"type": "INTEGER"
BIGINT	Large integer	"type": "BIGINT"
DECIMAL	Exact numeric	"type": "DECIMAL", "precision": 10, "scale": 2
NUMERIC	Exact numeric	"type": "NUMERIC", "precision": 10, "scale": 2
REAL	Single precision	"type": "REAL"
DOUBLE_PRECISION	Double precision	"type": "DOUBLE_PRECISION"
SERIAL	Auto-increment	"type": "SERIAL"
BIGSERIAL	Auto-increment big	"type": "BIGSERIAL"
Character Types
Type	Description	Usage
VARCHAR	Variable string	"type": "VARCHAR", "length": 255
CHAR	Fixed string	"type": "CHAR", "length": 10
TEXT	Unlimited string	"type": "TEXT"
Date/Time Types
Type	Description	Usage
DATE	Date only	"type": "DATE"
TIME	Time only	"type": "TIME"
TIMESTAMP	Date and time	"type": "TIMESTAMP"
TIMESTAMPTZ	Date/time with timezone	"type": "TIMESTAMPTZ"
Other Types
Type	Description	Usage
BOOLEAN	True/False	"type": "BOOLEAN"
JSON	JSON data	"type": "JSON"
JSONB	Binary JSON	"type": "JSONB"
UUID	UUID	"type": "UUID"
BYTEA	Binary data	"type": "BYTEA"
INET	IP address	"type": "INET"
Array Types
Type	Description	Usage
INTEGER_ARRAY	Integer array	"type": "INTEGER_ARRAY"
BIGINT_ARRAY	Big integer array	"type": "BIGINT_ARRAY"
VARCHAR_ARRAY	String array	"type": "VARCHAR_ARRAY"
TEXT_ARRAY	Text array	"type": "TEXT_ARRAY"
Column Properties
Property	Required	Description	Example
name	✅ Yes	Column name	"name": "user_id"
type	✅ Yes	Data type	"type": "BIGINT"
length	❌ No	Length for VARCHAR/CHAR	"length": 255
precision	❌ No	Precision for DECIMAL	"precision": 10
scale	❌ No	Scale for DECIMAL	"scale": 2
nullable	❌ No	Allow NULL (default: true)	"nullable": false
unique	❌ No	Column must be unique	"unique": true
defaultValue	❌ No	Default value	"defaultValue": "CURRENT_TIMESTAMP"
identity	❌ No	Auto-increment	"identity": true
identityStartWith	❌ No	Start value	"identityStartWith": 1000
identityIncrementBy	❌ No	Increment value	"identityIncrementBy": 1
comment	❌ No	Column comment	"comment": "User ID"
Constraints
Primary Key
json
"primaryKeys": [
{
"name": "pk_table_name",
"columns": ["column_name"]
}
]
Unique Key
json
"uniqueKeys": [
{
"name": "uk_table_name",
"columns": ["column_name"]
}
]
Foreign Key
json
"foreignKeys": [
{
"name": "fk_table_name",
"columns": ["column_name"],
"referencedTable": "referenced_table",
"referencedColumns": ["referenced_column"],
"onDelete": "CASCADE",
"onUpdate": "CASCADE"
}
]
On Delete/Update Actions:

NO ACTION - Prevent deletion if references exist

RESTRICT - Prevent deletion if references exist

CASCADE - Delete/update referenced rows

SET NULL - Set foreign key to NULL

SET DEFAULT - Set foreign key to default

Check Constraint
json
"checkConstraints": [
{
"name": "chk_table_name",
"expression": "column_name > 0"
}
]
Default Values
Default Value	Type	Works
"CURRENT_TIMESTAMP"	TIMESTAMP	✅
"NOW()"	TIMESTAMP	✅
"CURRENT_DATE"	DATE	✅
"CURRENT_TIME"	TIME	✅
"GEN_RANDOM_UUID()"	UUID	✅
"true" / "false"	BOOLEAN	✅
"0" / "100.50"	Numeric	✅
"'active'"	VARCHAR	✅
"UUID_GENERATE_V4()"	UUID	❌ Requires extension
Examples
1. Users Table
   bash
   curl -X POST http://localhost:8080/api/v1/schema/create \
   -H "Content-Type: application/json" \
   -d '{
   "schema": "public",
   "tableName": "users",
   "ifNotExists": true,
   "tableComment": "User accounts",
   "columns": [
   {
   "name": "user_id",
   "type": "UUID",
   "nullable": false,
   "defaultValue": "GEN_RANDOM_UUID()"
   },
   {
   "name": "username",
   "type": "VARCHAR",
   "length": 50,
   "nullable": false,
   "unique": true
   },
   {
   "name": "email",
   "type": "VARCHAR",
   "length": 255,
   "nullable": false,
   "unique": true
   },
   {
   "name": "created_at",
   "type": "TIMESTAMP",
   "nullable": false,
   "defaultValue": "CURRENT_TIMESTAMP"
   },
   {
   "name": "is_active",
   "type": "BOOLEAN",
   "nullable": false,
   "defaultValue": "true"
   }
   ],
   "constraints": {
   "primaryKeys": [
   {"name": "pk_users", "columns": ["user_id"]}
   ]
   }
   }'
2. Products Table with Check Constraints
   bash
   curl -X POST http://localhost:8080/api/v1/schema/create \
   -H "Content-Type: application/json" \
   -d '{
   "schema": "public",
   "tableName": "products",
   "columns": [
   {
   "name": "product_id",
   "type": "BIGINT",
   "nullable": false,
   "identity": true
   },
   {
   "name": "product_name",
   "type": "VARCHAR",
   "length": 200,
   "nullable": false
   },
   {
   "name": "price",
   "type": "DECIMAL",
   "precision": 10,
   "scale": 2,
   "nullable": false
   },
   {
   "name": "quantity",
   "type": "INTEGER",
   "nullable": false
   },
   {
   "name": "status",
   "type": "VARCHAR",
   "length": 20,
   "nullable": false,
   "defaultValue": "'in_stock'"
   }
   ],
   "constraints": {
   "primaryKeys": [
   {"name": "pk_products", "columns": ["product_id"]}
   ],
   "checkConstraints": [
   {"name": "chk_price", "expression": "price >= 0"},
   {"name": "chk_quantity", "expression": "quantity >= 0"},
   {"name": "chk_status", "expression": "status IN ('in_stock', 'out_of_stock', 'discontinued')"}
   ]
   }
   }'
3. Orders with Foreign Key
   bash
# Create customers first
curl -X POST http://localhost:8080/api/v1/schema/create \
-H "Content-Type: application/json" \
-d '{
"schema": "public",
"tableName": "customers",
"columns": [
{"name": "customer_id", "type": "BIGINT", "nullable": false, "identity": true},
{"name": "customer_name", "type": "VARCHAR", "length": 200, "nullable": false}
],
"constraints": {
"primaryKeys": [{"name": "pk_customers", "columns": ["customer_id"]}]
}
}'

# Create orders with foreign key
curl -X POST http://localhost:8080/api/v1/schema/create \
-H "Content-Type: application/json" \
-d '{
"schema": "public",
"tableName": "orders",
"columns": [
{"name": "order_id", "type": "BIGINT", "nullable": false, "identity": true},
{"name": "customer_id", "type": "BIGINT", "nullable": false},
{"name": "total_amount", "type": "DECIMAL", "precision": 15, "scale": 2, "nullable": false},
{"name": "order_date", "type": "TIMESTAMP", "nullable": false, "defaultValue": "CURRENT_TIMESTAMP"}
],
"constraints": {
"primaryKeys": [{"name": "pk_orders", "columns": ["order_id"]}],
"foreignKeys": [
{
"name": "fk_orders_customer",
"columns": ["customer_id"],
"referencedTable": "customers",
"referencedColumns": ["customer_id"],
"onDelete": "RESTRICT",
"onUpdate": "CASCADE"
}
]
}
}'
4. Simple Table (No Constraints)
   bash
   curl -X POST http://localhost:8080/api/v1/schema/create \
   -H "Content-Type: application/json" \
   -d '{
   "schema": "public",
   "tableName": "audit_log",
   "columns": [
   {"name": "id", "type": "BIGINT", "identity": true},
   {"name": "action", "type": "VARCHAR", "length": 50, "nullable": false},
   {"name": "details", "type": "JSONB"},
   {"name": "created_at", "type": "TIMESTAMP", "defaultValue": "CURRENT_TIMESTAMP"}
   ],
   "tableComment": "Audit log"
   }'
5. Temporary Table
   bash
   curl -X POST http://localhost:8080/api/v1/schema/create-and-register \
   -H "Content-Type: application/json" \
   -d '{
   "schema": "public",
   "tableName": "temp_import",
   "temporary": true,
   "version":
   "columns": [
   {"name": "row_id", "type": "INTEGER"},
   {"name": "data", "type": "TEXT"},
   {"name": "import_date", "type": "TIMESTAMP", "defaultValue": "CURRENT_TIMESTAMP"}
   ]
   }'
6. Using the API
   List Tables:

bash
curl "http://localhost:8080/api/v1/schema/list?schema=public"
Get Table Info:

bash
curl "http://localhost:8080/api/v1/schema/info?schema=public&tableName=users"
Check if Table Exists:

bash
curl "http://localhost:8080/api/v1/schema/exists?schema=public&tableName=users"
Drop Table:

bash
curl -X DELETE http://localhost:8080/api/v1/schema/drop \
-H "Content-Type: application/json" \
-d '{"schema": "public", "tableName": "users"}'
Truncate Table:

bash
curl -X DELETE http://localhost:8080/api/v1/schema/truncate \
-H "Content-Type: application/json" \
-d '{"schema": "public", "tableName": "users"}'
Health Check:

bash
curl http://localhost:8080/api/v1/actuator/health
Error Handling
Common Errors and Solutions
Error	Cause	Solution
Table already exists	Table with same name exists	Use "ifNotExists": true
Invalid timestamp format	Wrong timestamp value	Use CURRENT_TIMESTAMP without quotes
UUID function not found	Extension not enabled	Use GEN_RANDOM_UUID()
uniqueKeys cannot be null	Missing constraints	Use "constraints": {}
Constraint name already exists	Duplicate name	Use unique names
Best Practices
Naming Conventions
Tables: lowercase with underscores (e.g., order_items)

Columns: lowercase with underscores (e.g., first_name)

Constraints: prefix with type (e.g., pk_users, fk_orders_customer)

Data Type Recommendations
Use Case	Recommended Type
Primary Key (small)	SERIAL or INTEGER with identity
Primary Key (large)	BIGSERIAL or BIGINT with identity
Primary Key (distributed)	UUID with GEN_RANDOM_UUID()
Names, Titles	VARCHAR(255)
Long Text	TEXT
Currency	DECIMAL(10,2)
Counts, IDs	BIGINT
True/False	BOOLEAN
Dates	TIMESTAMP
JSON Data	JSONB
Security Best Practices
Use RESTRICT on delete for important relationships

Validate input before sending to API

Use UUID for public-facing IDs

Never expose internal IDs

Support
Health Check: curl http://localhost:8080/api/v1/actuator/health

Logs: Check logs/application.log for detailed debugging information.

text

## 2. API Documentation - Markdown File

Thanks for reading