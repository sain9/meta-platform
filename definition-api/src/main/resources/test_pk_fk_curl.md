# Primary Key & Foreign Key Test Examples

This document provides `curl` examples for creating tables with Primary Keys and Foreign Keys using the Definition API.

---

## 1. Create Department Table (Parent Table)

```bash
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
```

**Generated SQL:**
```sql
CREATE TABLE public.department (
    department_id BIGINT NOT NULL,
    department_name VARCHAR(100) NOT NULL,
    location VARCHAR(200),
    budget DECIMAL(15,2),
    CONSTRAINT pk_department PRIMARY KEY (department_id),
    CONSTRAINT uk_department_name UNIQUE (department_name)
);
COMMENT ON TABLE public.department IS 'Department information table';
```

---

## 2. Create Employee Table with Foreign Key (Child Table)

```bash
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
```

**Generated SQL:**
```sql
CREATE TABLE public.employee (
    employee_id BIGINT NOT NULL,
    employee_name VARCHAR(100) NOT NULL,
    department_id BIGINT NOT NULL,
    salary DECIMAL(10,2),
    hire_date DATE,
    CONSTRAINT pk_employee PRIMARY KEY (employee_id),
    CONSTRAINT fk_employee_department FOREIGN KEY (department_id)
        REFERENCES public.department (department_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT uk_employee_name_department UNIQUE (employee_name, department_id)
);
COMMENT ON TABLE public.employee IS 'Employee information table with foreign key';
```

---

## Foreign Key Actions Reference

| Action | Behavior |
|--------|----------|
| `NO ACTION` | Prevent deletion if references exist |
| `RESTRICT` | Prevent deletion if references exist (checked immediately) |
| `CASCADE` | Delete/update referenced rows automatically |
| `SET NULL` | Set foreign key to NULL |
| `SET DEFAULT` | Set foreign key to default value |

---

## Verification Commands

### List Tables
```bash
curl "http://localhost:8080/api/v1/schema/list?schema=public"
```

### Get Table Info
```bash
curl "http://localhost:8080/api/v1/schema/info?schema=public&tableName=employee"
```

### Health Check
```bash
curl http://localhost:8080/actuator/health
```