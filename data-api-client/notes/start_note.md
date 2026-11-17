# Startup Guide

## Build Order

1.  Build `data-api-proto`
2.  Build `data-api` (uses `data-api-proto`)
3.  Build `data-api-client` (uses `data-api-proto`)

## Running Tests

Navigate to the test directory to inspect the available unit tests:

```bash
check test/java/com.hussain.data_api_client
```

Two test classes are available:
*   `ClientSpringContextTest`
*   `ClientSmokeTest`

### Configure Tests

Ensure the entity and columns you use in the tests actually exist in your database.

### Run Tests

Execute either test class:

```bash
# Run Spring Context Test
mvn clean test -Dtest=ClientSpringContextTest

# OR Run Smoke Test
mvn test -Dtest=ClientSmokeTest
```

### Expected Output

```
Service/Execute OK in 212ms
AUTO-CONFIG OK: {id=d14cfb36-0bfe-4f1e-84d0-b32e37f4147d, entity_definition= ...}
```