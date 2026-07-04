DUMMY TABLE TO TEST DATA API
----------------------------
-- Create table
CREATE TABLE parts (
name VARCHAR(255) NOT NULL,
partowner VARCHAR(255) NOT NULL,
description TEXT,
quantity INT NOT NULL
);

-- Insert data
INSERT INTO parts (name, partowner, description, quantity)
VALUES ('New Widget Part', 'John Doe', 'A high-quality widget part', 100);
----------------------------------------------------------------------------


# READ with where clause
curl -X POST http://localhost:8081/api/data/execute \
-H "Content-Type: application/json" \
-d '{
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
}'

## above will form such query
(SELECT name, partowner, quantity FROM parts WHERE partowner = 'John Doe')

# SIMPLE READ 

curl -X POST http://localhost:8081/api/data/execute \
-H "Content-Type: application/json" \
-d '{
"operation": "READ",
"entity": "parts",
"columns": ["name", "partowner", "quantity"]
}'

(SELECT name, partowner, quantity FROM parts)

# ADVANCE READ
curl -X POST http://localhost:8081/api/data/execute \
-H "Content-Type: application/json" \
-d '{
"operation": "READ",
"entity": "parts",
"columns": ["name", "partowner", "description", "quantity"],
"filters": [
{
"column": "name",
"operator": "LIKE",
"value": "Widget"
}
]
}'

## above will form such query
(SELECT name, partowner, description, quantity FROM parts WHERE name LIKE '%Widget%')

# READ with IN operator
curl -X POST http://localhost:8081/api/data/execute \
-H "Content-Type: application/json" \
-d '{
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
}'

## above will form such query
(SELECT name, partowner, quantity FROM parts WHERE partowner IN ('John Doe', 'Alice Smith', 'Carol White') )

#  READ with BETWEEN operator
curl -X POST http://localhost:8081/api/data/execute \
-H "Content-Type: application/json" \
-d '{
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
}'

## above will form such query
(SELECT name, partowner, quantity FROM parts WHERE quantity BETWEEN 50 AND 200)

# READ with IS_NULL and IS_NOT_NULL and greater than
curl -X POST http://localhost:8081/api/data/execute \
-H "Content-Type: application/json" \
-d '{
"operation": "READ",
"entity": "parts",
"columns": ["name", "partowner", "description", "quantity"],
"filters": [
{
"column": "description",
"operator": "IS_NOT_NULL",
"logicalOperator": "AND"
},
{
"column": "quantity",
"operator": "GT",
"value": 50,
"logicalOperator": "AND"
}
]
}'

## above will form such query
(SELECT name, partowner, description, quantity FROM parts WHERE description IS NOT NULL AND quantity > 50)


# READ WITH ORDERBY
curl -X POST http://localhost:8081/api/data/execute \
-H "Content-Type: application/json" \
-d '{
"operation": "READ",
"entity": "parts",
"columns": ["name", "partowner", "quantity"],
"orderBy": [
{
"column": "quantity",
"direction": "DESC"
}
]
}'

## above will form such query
(SELECT name, partowner, quantity FROM parts ORDER BY quantity DESC)

# READ with GROUP BY and HAVING
curl -X POST http://localhost:8081/api/data/execute \
-H "Content-Type: application/json" \
-d '{
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
],
"orderBy": [
{
"column": "total_quantity",
"direction": "DESC"
}
]
}'

## above will form such query
(SELECT partowner, COUNT(name) as total_parts, SUM(quantity) as total_quantity FROM parts GROUP BY partowner HAVING SUM(quantity) > 100 ORDER BY total_quantity DESC)



-------------------------------------------------------------------------------------------------------------------------------------------------------------------------

# INSERT

curl -X POST http://localhost:8081/api/data/execute \
-H "Content-Type: application/json" \
-d '{
"operation": "CREATE",
"entity": "parts",
"data": {
"name": "Premium Widget",
"partowner": "Alice Smith",
"description": "Premium quality widget",
"quantity": 50
}
}'



#  Batch Operations (Multiple operations in one request)
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
"description": "Created via batch operation",
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