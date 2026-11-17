# Data API Test Notes

This document provides quick reference examples for testing the Data API using `curl`.

---

## Dummy Table for Testing

### Schema

```sql
CREATE TABLE parts (
    name VARCHAR(255) NOT NULL,
    partowner VARCHAR(255) NOT NULL,
    description TEXT,
    quantity INT NOT NULL
);
```

### Sample Data

```sql
INSERT INTO parts (name, partowner, description, quantity)
VALUES ('New Widget Part', 'John Doe', 'A high-quality widget part', 100);
```

---

## READ Operations

### Simple READ (All Columns)

```bash
curl -X POST http://localhost:8081/api/data/execute \
  -H "Content-Type: application/json" \
  -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"]
  }'
```

**Generated SQL:**
```sql
SELECT name, partowner, quantity FROM parts
```

---

### READ with WHERE Clause

```bash
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
```

**Generated SQL:**
```sql
SELECT name, partowner, quantity FROM parts WHERE partowner = 'John Doe'
```

---

### ADVANCED READ with LIKE

```bash
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
```

**Generated SQL:**
```sql
SELECT name, partowner, description, quantity FROM parts WHERE name LIKE '%Widget%'
```

---

### READ with IN Operator

```bash
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
```

**Generated SQL:**
```sql
SELECT name, partowner, quantity FROM parts WHERE partowner IN ('John Doe', 'Alice Smith', 'Carol White')
```

---

### READ with BETWEEN Operator

```bash
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
```

**Generated SQL:**
```sql
SELECT name, partowner, quantity FROM parts WHERE quantity BETWEEN 50 AND 200
```

---

### READ with IS_NULL, IS_NOT_NULL and GT

```bash
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
```

**Generated SQL:**
```sql
SELECT name, partowner, description, quantity FROM parts WHERE description IS NOT NULL AND quantity > 50
```

---

### READ with ORDER BY

```bash
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
```

**Generated SQL:**
```sql
SELECT name, partowner, quantity FROM parts ORDER BY quantity DESC
```

---

### READ with GROUP BY and HAVING

```bash
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
```

**Generated SQL:**
```sql
SELECT partowner, COUNT(name) as total_parts, SUM(quantity) as total_quantity
FROM parts
GROUP BY partowner
HAVING SUM(quantity) > 100
ORDER BY total_quantity DESC
```

---

## CREATE (INSERT) Operation

```bash
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
```

---

## Batch Operations

Multiple operations in a single request:

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
```

---

## JOINS, SUBQUERY, Nested Subquery

### Example 1: Complex LEFT JOIN with Subquery

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

**Expected SQL:**
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

---

### Example 2: Simple LEFT JOIN

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

**Expected SQL:**
```sql
SELECT p.name
FROM parts p
LEFT JOIN suppliers s ON s.part_name = p.name
WHERE s.part_name IS NULL
```

---

### Example 3: Nested Subquery (4 Levels Deep)

```bash
curl -X POST http://localhost:9995/api/data/execute \
  -H "Content-Type: application/json" \
  -d '{
    "operation": "READ",
    "from": { "entity": "parts", "alias": "p" },
    "columns": ["p.name"],
    "filters": [{
      "column": "p.name",
      "operator": "IN",
      "subquery": {
        "query": {
          "operation": "READ",
          "from": { "entity": "suppliers", "alias": "s" },
          "columns": ["s.part_name"],
          "filters": [{
            "column": "s.part_name",
            "operator": "IN",
            "subquery": {
              "query": {
                "operation": "READ",
                "from": { "entity": "orders", "alias": "o" },
                "columns": ["o.part_name"],
                "filters": [{
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
                }]
              }
            }
          }]
        }
      }
    }]
  }'
```

**Expected SQL:**
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