# First create department table
curl -X POST http://localhost:8080/api/v1/schema/create \
-H "Content-Type: application/json" \
-d '{
"schema": "public",
"tableName": "department",
"columns": [
{
"name": "department_id",
"type": "BIGINT",
"nullable": false
},
{
"name": "department_name",
"type": "VARCHAR",
"length": 100,
"nullable": false
},
{
"name": "location",
"type": "VARCHAR",
"length": 200
},
{
"name": "budget",
"type": "DECIMAL",
"precision": 15,
"scale": 2
}
],
"constraints": {
"primaryKeys": [
{
"name": "pk_department",
"columns": ["department_id"]
}
],
"uniqueKeys": [
{
"name": "uk_department_name",
"columns": ["department_name"]
}
]
},
"tableComment": "Department information table"
}'

############################################################################

# Create employee with foreign key
curl -X POST http://localhost:8080/api/v1/schema/create \
-H "Content-Type: application/json" \
-d '{
"schema": "public",
"tableName": "employee",
"columns": [
{
"name": "employee_id",
"type": "BIGINT",
"nullable": false
},
{
"name": "employee_name",
"type": "VARCHAR",
"length": 100,
"nullable": false
},
{
"name": "department_id",
"type": "BIGINT",
"nullable": false
},
{
"name": "salary",
"type": "DECIMAL",
"precision": 10,
"scale": 2
},
{
"name": "hire_date",
"type": "DATE"
}
],
"constraints": {
"primaryKeys": [
{
"name": "pk_employee",
"columns": ["employee_id"]
}
],
"foreignKeys": [
{
"name": "fk_employee_department",
"columns": ["department_id"],
"referencedTable": "department",
"referencedColumns": ["department_id"],
"onDelete": "RESTRICT",
"onUpdate": "CASCADE"
}
],
"uniqueKeys": [
{
"name": "uk_employee_name_department",
"columns": ["employee_name", "department_id"]
}
]
},
"tableComment": "Employee information table with foreign key"
}'