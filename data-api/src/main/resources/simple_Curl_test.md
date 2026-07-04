# Test READ
curl -X POST http://localhost:8081/api/data/execute \
  -H "Content-Type: application/json" \
  -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"]
  }'

# Test CREATE
curl -X POST http://localhost:8081/api/data/execute \
  -H "Content-Type: application/json" \
  -d '{
    "operation": "CREATE",
    "entity": "parts",
    "data": {
      "name": "Test Part 2",
      "partowner": "Test Owner",
      "description": "Test description",
      "quantity": 75
    }
  }'

# Test UPDATE
curl -X POST http://localhost:8081/api/data/execute \
  -H "Content-Type: application/json" \
  -d '{
    "operation": "UPDATE",
    "entity": "parts",
    "data": {
      "quantity": 200
    },
    "filters": [
      {
        "column": "name",
        "operator": "EQ",
        "value": "Test Part 2"
      }
    ]
  }'

# Test DELETE
curl -X POST http://localhost:8081/api/data/execute \
-H "Content-Type: application/json" \
-d '{
"operation": "DELETE",
"entity": "parts",
"filters": [
{
"column": "name",
"operator": "EQ",
"value": "Test Part 2"
}
]
}'