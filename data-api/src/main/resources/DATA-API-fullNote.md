Here are all the possible curl requests with the jOOQ SQL that will be formed for each operation using the "parts" entity:

1. CREATE (Insert) - Single Record
   Curl Command:
   bash
   curl -X POST http://localhost:8081/api/data/execute \
   -H "Content-Type: application/json" \
   -d '{
   "operation": "CREATE",
   "entity": "parts",
   "data": {
   "name": "New Widget Part",
   "partowner": "John Doe",
   "description": "A high-quality widget part",
   "quantity": 100
   }
   }'
   Generated SQL:
   sql
   INSERT INTO parts (name, partowner, description, quantity)
   VALUES ('New Widget Part', 'John Doe', 'A high-quality widget part', 100)
2. CREATE - With NULL values
   Curl Command:
   bash
   curl -X POST http://localhost:8081/api/data/execute \
   -H "Content-Type: application/json" \
   -d '{
   "operation": "CREATE",
   "entity": "parts",
   "data": {
   "name": "Part With Null Desc",
   "partowner": "Jane Smith",
   "description": null,
   "quantity": 50
   }
   }'
   Generated SQL:
   sql
   INSERT INTO parts (name, partowner, description, quantity)
   VALUES ('Part With Null Desc', 'Jane Smith', NULL, 50)
3. READ - Select all columns
   Curl Command:
   bash
   curl -X POST http://localhost:8081/api/data/execute \
   -H "Content-Type: application/json" \
   -d '{
   "operation": "READ",
   "entity": "parts",
   "columns": ["name", "partowner", "description", "quantity"]
   }'
   Generated SQL:
   sql
   SELECT name, partowner, description, quantity FROM parts
4. READ - Select specific columns
   Curl Command:
   bash
   curl -X POST http://localhost:8081/api/data/execute \
   -H "Content-Type: application/json" \
   -d '{
   "operation": "READ",
   "entity": "parts",
   "columns": ["name", "quantity"]
   }'
   Generated SQL:
   sql
   SELECT name, quantity FROM parts
5. READ - With WHERE EQ filter
   Curl Command:
   bash
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
   Generated SQL:
   sql
   SELECT name, partowner, quantity FROM parts WHERE partowner = 'John Doe'
6. READ - With WHERE NEQ filter
   Curl Command:
   bash
   curl -X POST http://localhost:8081/api/data/execute \
   -H "Content-Type: application/json" \
   -d '{
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
   }'
   Generated SQL:
   sql
   SELECT name, partowner, quantity FROM parts WHERE partowner <> 'John Doe'
7. READ - With WHERE GT (Greater Than) filter
   Curl Command:
   bash
   curl -X POST http://localhost:8081/api/data/execute \
   -H "Content-Type: application/json" \
   -d '{
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
   }'
   Generated SQL:
   sql
   SELECT name, partowner, quantity FROM parts WHERE quantity > 50
8. READ - With WHERE GTE (Greater Than or Equal) filter
   Curl Command:
   bash
   curl -X POST http://localhost:8081/api/data/execute \
   -H "Content-Type: application/json" \
   -d '{
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
   }'
   Generated SQL:
   sql
   SELECT name, partowner, quantity FROM parts WHERE quantity >= 100
9. READ - With WHERE LT (Less Than) filter
   Curl Command:
   bash
   curl -X POST http://localhost:8081/api/data/execute \
   -H "Content-Type: application/json" \
   -d '{
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
   }'
   Generated SQL:
   sql
   SELECT name, partowner, quantity FROM parts WHERE quantity < 100
10. READ - With WHERE LTE (Less Than or Equal) filter
    Curl Command:
    bash
    curl -X POST http://localhost:8081/api/data/execute \
    -H "Content-Type: application/json" \
    -d '{
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
    }'
    Generated SQL:
    sql
    SELECT name, partowner, quantity FROM parts WHERE quantity <= 100
11. READ - With WHERE LIKE filter
    Curl Command:
    bash
    curl -X POST http://localhost:8081/api/data/execute \
    -H "Content-Type: application/json" \
    -d '{
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
    }'
    Generated SQL:
    sql
    SELECT name, partowner, quantity FROM parts WHERE name LIKE '%Widget%'
12. READ - With WHERE ILIKE filter (Case-insensitive)
    Curl Command:
    bash
    curl -X POST http://localhost:8081/api/data/execute \
    -H "Content-Type: application/json" \
    -d '{
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
    }'
    Generated SQL:
    sql
    SELECT name, partowner, quantity FROM parts WHERE name ILIKE '%widget%'
13. READ - With WHERE IN filter
    Curl Command:
    bash
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
    Generated SQL:
    sql
    SELECT name, partowner, quantity FROM parts WHERE partowner IN ('John Doe', 'Alice Smith', 'Carol White')
14. READ - With WHERE NOT IN filter
    Curl Command:
    bash
    curl -X POST http://localhost:8081/api/data/execute \
    -H "Content-Type: application/json" \
    -d '{
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
    }'
    Generated SQL:
    sql
    SELECT name, partowner, quantity FROM parts WHERE partowner NOT IN ('John Doe', 'Alice Smith')
15. READ - With WHERE IS NULL filter
    Curl Command:
    bash
    curl -X POST http://localhost:8081/api/data/execute \
    -H "Content-Type: application/json" \
    -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "description", "quantity"],
    "filters": [
    {
    "column": "description",
    "operator": "IS_NULL"
    }
    ]
    }'
    Generated SQL:
    sql
    SELECT name, partowner, description, quantity FROM parts WHERE description IS NULL
16. READ - With WHERE IS NOT NULL filter
    Curl Command:
    bash
    curl -X POST http://localhost:8081/api/data/execute \
    -H "Content-Type: application/json" \
    -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "description", "quantity"],
    "filters": [
    {
    "column": "description",
    "operator": "IS_NOT_NULL"
    }
    ]
    }'
    Generated SQL:
    sql
    SELECT name, partowner, description, quantity FROM parts WHERE description IS NOT NULL
17. READ - With WHERE BETWEEN filter
    Curl Command:
    bash
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
    Generated SQL:
    sql
    SELECT name, partowner, quantity FROM parts WHERE quantity BETWEEN 50 AND 200
18. READ - With WHERE CONTAINS filter
    Curl Command:
    bash
    curl -X POST http://localhost:8081/api/data/execute \
    -H "Content-Type: application/json" \
    -d '{
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
    }'
    Generated SQL:
    sql
    SELECT name, partowner, description FROM parts WHERE description LIKE '%quality%'
19. READ - With WHERE STARTS_WITH filter
    Curl Command:
    bash
    curl -X POST http://localhost:8081/api/data/execute \
    -H "Content-Type: application/json" \
    -d '{
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
    }'
    Generated SQL:
    sql
    SELECT name, partowner FROM parts WHERE name LIKE 'New%'
20. READ - With WHERE ENDS_WITH filter
    Curl Command:
    bash
    curl -X POST http://localhost:8081/api/data/execute \
    -H "Content-Type: application/json" \
    -d '{
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
    }'
    Generated SQL:
    sql
    SELECT name, partowner FROM parts WHERE name LIKE '%Widget'
21. READ - With multiple filters (AND)
    Curl Command:
    bash
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
    }'
    Generated SQL:
    sql
    SELECT name, partowner, quantity FROM parts WHERE partowner = 'John Doe' AND quantity > 50
22. READ - With multiple filters (OR)
    Curl Command:
    bash
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
    }'
    Generated SQL:
    sql
    SELECT name, partowner, quantity FROM parts WHERE partowner = 'John Doe' OR partowner = 'Alice Smith'
23. READ - With ORDER BY ASC
    Curl Command:
    bash
    curl -X POST http://localhost:8081/api/data/execute \
    -H "Content-Type: application/json" \
    -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"],
    "orderBy": [
    {
    "column": "name",
    "direction": "ASC"
    }
    ]
    }'
    Generated SQL:
    sql
    SELECT name, partowner, quantity FROM parts ORDER BY name ASC
24. READ - With ORDER BY DESC
    Curl Command:
    bash
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
    Generated SQL:
    sql
    SELECT name, partowner, quantity FROM parts ORDER BY quantity DESC
25. READ - With multiple ORDER BY
    Curl Command:
    bash
    curl -X POST http://localhost:8081/api/data/execute \
    -H "Content-Type: application/json" \
    -d '{
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
    }'
    Generated SQL:
    sql
    SELECT name, partowner, quantity FROM parts ORDER BY partowner ASC, quantity DESC
26. READ - With LIMIT only
    Curl Command:
    bash
    curl -X POST http://localhost:8081/api/data/execute \
    -H "Content-Type: application/json" \
    -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"],
    "limit": 5
    }'
    Generated SQL:
    sql
    SELECT name, partowner, quantity FROM parts LIMIT 5
27. READ - With LIMIT and OFFSET (Pagination)
    Curl Command:
    bash
    curl -X POST http://localhost:8081/api/data/execute \
    -H "Content-Type: application/json" \
    -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["name", "partowner", "quantity"],
    "limit": 5,
    "offset": 10
    }'
    Generated SQL:
    sql
    SELECT name, partowner, quantity FROM parts LIMIT 5 OFFSET 10
28. READ - With GROUP BY
    Curl Command:
    bash
    curl -X POST http://localhost:8081/api/data/execute \
    -H "Content-Type: application/json" \
    -d '{
    "operation": "READ",
    "entity": "parts",
    "columns": ["partowner", "COUNT(name) as total_parts"],
    "groupBy": [
    {
    "column": "partowner"
    }
    ]
    }'
    Generated SQL:
    sql
    SELECT partowner, COUNT(name) as total_parts FROM parts GROUP BY partowner
29. READ - With GROUP BY and HAVING
    Curl Command:
    bash
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
    ]
    }'
    Generated SQL:
    sql
    SELECT partowner, COUNT(name) as total_parts, SUM(quantity) as total_quantity
    FROM parts
    GROUP BY partowner
    HAVING SUM(quantity) > 100
30. READ - With YEAR GROUP BY
    Curl Command:
    bash
    curl -X POST http://localhost:8081/api/data/execute \
    -H "Content-Type: application/json" \
    -d '{
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
    }'
    Generated SQL:
    sql
    SELECT EXTRACT(YEAR FROM created_at) as year, COUNT(name) as total_parts
    FROM parts
    GROUP BY EXTRACT(YEAR FROM created_at)
31. READ - With DATE_TRUNC GROUP BY
    Curl Command:
    bash
    curl -X POST http://localhost:8081/api/data/execute \
    -H "Content-Type: application/json" \
    -d '{
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
    }'
    Generated SQL:
    sql
    SELECT DATE_TRUNC('month', created_at) as month, COUNT(name) as total_parts
    FROM parts
    GROUP BY DATE_TRUNC('month', created_at)
32. UPDATE - Single column
    Curl Command:
    bash
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
    "value": "New Widget Part"
    }
    ]
    }'
    Generated SQL:
    sql
    UPDATE parts SET quantity = 200 WHERE name = 'New Widget Part'
33. UPDATE - Multiple columns
    Curl Command:
    bash
    curl -X POST http://localhost:8081/api/data/execute \
    -H "Content-Type: application/json" \
    -d '{
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
    }'
    Generated SQL:
    sql
    UPDATE parts
    SET quantity = 150, description = 'Updated description - Premium quality', partowner = 'Jane Smith'
    WHERE name = 'Premium Widget'
34. DELETE - With single filter
    Curl Command:
    bash
    curl -X POST http://localhost:8081/api/data/execute \
    -H "Content-Type: application/json" \
    -d '{
    "operation": "DELETE",
    "entity": "parts",
    "filters": [
    {
    "column": "name",
    "operator": "EQ",
    "value": "Lightweight Nut"
    }
    ]
    }'
    Generated SQL:
    sql
    DELETE FROM parts WHERE name = 'Lightweight Nut'
35. DELETE - With multiple filters
    Curl Command:
    bash
    curl -X POST http://localhost:8081/api/data/execute \
    -H "Content-Type: application/json" \
    -d '{
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
    }'
    Generated SQL:
    sql
    DELETE FROM parts WHERE partowner = 'Bob Johnson' AND quantity < 100
36. BULK CREATE - Multiple records
    Curl Command:
    bash
    curl -X POST http://localhost:8081/api/data/execute \
    -H "Content-Type: application/json" \
    -d '{
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
    }'
    Generated SQL:
    sql
    INSERT INTO parts (name, partowner, description, quantity) VALUES ('Standard Bolt', 'Bob Johnson', 'Standard grade bolt', 200);
    INSERT INTO parts (name, partowner, description, quantity) VALUES ('Heavy Duty Screw', 'Carol White', 'Heavy duty screw', 150);
    INSERT INTO parts (name, partowner, description, quantity) VALUES ('Lightweight Nut', 'David Brown', 'Lightweight nut', 300);
    (Executed as a batch)

37. BULK UPDATE - Multiple records
    Curl Command:
    bash
    curl -X POST http://localhost:8081/api/data/execute \
    -H "Content-Type: application/json" \
    -d '{
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
    }'
    Generated SQL:
    sql
    UPDATE parts SET quantity = 250, description = 'Bulk updated' WHERE name = 'New Widget Part';
    UPDATE parts SET quantity = 350, description = 'Bulk updated - Premium' WHERE name = 'Premium Widget';
    (Executed as a batch)

38. BULK DELETE - Multiple IDs
    Curl Command:
    bash
    curl -X POST http://localhost:8081/api/data/execute \
    -H "Content-Type: application/json" \
    -d '{
    "operation": "BULK_DELETE",
    "entity": "parts",
    "data": {
    "ids": [1, 2, 3]
    }
    }'
    Generated SQL:
    sql
    DELETE FROM parts WHERE id IN (1, 2, 3)
39. BATCH - Multiple operations
    Curl Command:
    bash
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
    Generated SQL:
    sql
    -- Operation 1:
    SELECT name, partowner FROM parts LIMIT 5

-- Operation 2:
INSERT INTO parts (name, partowner, description, quantity)
VALUES ('Batch Created Part', 'System User', 'Created via batch', 25)

-- Operation 3:
SELECT name, partowner, quantity FROM parts WHERE partowner = 'System User'
40. Health Check
    Curl Command:
    bash
    curl -X GET http://localhost:8081/api/data/health
    Response:
    json
    {"status":"UP","service":"data-api"}
    Complete Table Schema Used
    sql
    CREATE TABLE parts (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    partowner VARCHAR(255) NOT NULL,
    description TEXT,
    quantity INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    Sample Data for Testing
    sql
    INSERT INTO parts (name, partowner, description, quantity) VALUES
    ('New Widget Part', 'John Doe', 'A high-quality widget part', 100),
    ('Premium Widget', 'Alice Smith', 'Premium quality widget', 50),
    ('Standard Bolt', 'Bob Johnson', 'Standard grade bolt', 200),
    ('Heavy Duty Screw', 'Carol White', 'Heavy duty screw for industrial use', 150),
    ('Lightweight Nut', 'David Brown', 'Lightweight nut for precision work', 300),
    ('Super Widget', 'John Doe', 'Super quality widget', 75),
    ('Mini Widget', 'Alice Smith', 'Mini widget for small applications', 25);
