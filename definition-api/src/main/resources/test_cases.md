#unique constraint column without adding unique in constraint section

curl -X POST http://localhost:8080/api/v1/schema/create \
-H "Content-Type: application/json" \
-d '{
"schema": "public",
"tableName": "unique_only_table",
"columns": [
{
"name": "id",
"type": "BIGINT"
},
{
"name": "email",
"type": "VARCHAR",
"length": 255,
"unique": true
},
{
"name": "username",
"type": "VARCHAR",
"length": 50,
"unique": true
},
{
"name": "name",
"type": "VARCHAR",
"length": 100
}
],
"constraints": {},
"tableComment": "Table with unique columns but no primary key"
}'

##################################################################################
# Test with CURRENT_TIMESTAMP
curl -X POST http://localhost:8080/api/v1/schema/create \
-H "Content-Type: application/json" \
-d '{
"schema": "public",
"tableName": "required_fields_table",
"columns": [
{
"name": "id",
"type": "BIGINT",
"nullable": false
},
{
"name": "name",
"type": "VARCHAR",
"length": 100,
"nullable": false
},
{
"name": "email",
"type": "VARCHAR",
"length": 255,
"nullable": false
},
{
"name": "created_at",
"type": "TIMESTAMP",
"nullable": false,
"defaultValue": "CURRENT_TIMESTAMP"
}
],
"tableComment": "Table with NOT NULL constraints only"
}'

#######################################################################

# Test with NOW()
curl -X POST http://localhost:8080/api/v1/schema/create \
-H "Content-Type: application/json" \
-d '{
"schema": "public",
"tableName": "test_now",
"columns": [
{
"name": "id",
"type": "BIGINT",
"nullable": false
},
{
"name": "created_at",
"type": "TIMESTAMP",
"defaultValue": "NOW()"
}
]
}'

##################################################################################

# Test with UUID generation
curl -X POST http://localhost:8080/api/v1/schema/create \
-H "Content-Type: application/json" \
-d '{
"schema": "public",
"tableName": "test_uuid",
"columns": [
{
"name": "id",
"type": "UUID",
"nullable": false,
"defaultValue": "GEN_RANDOM_UUID()"
},
{
"name": "name",
"type": "VARCHAR",
"length": 100,
"nullable": false
}
],
"constraints": {
"primaryKeys": [
{
"name": "pk_test_uuid",
"columns": ["id"]
}
]
}
}'