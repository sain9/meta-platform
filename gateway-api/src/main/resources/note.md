# Gateway API Project Structure

```
gateway-api/
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.hussain.gateway_api
│   │   │       ├── GatewayApiApplication.java
│   │   │       ├── config
│   │   │       ├── filter
│   │   │       ├── dto
│   │   │       ├── util
│   │   │       └── exception
│   │   │
│   │   └── resources
│   │       └── application.yml
│   │
│   └── test
```

## Key Components

| Package | Description |
|---------|-------------|
| `config` | Spring configuration classes |
| `filter` | Gateway filters (authentication, routing) |
| `dto` | Data Transfer Objects |
| `util` | Utility classes |
| `exception` | Custom exception handlers |

## Build & Run

```bash
# From project root
mvn clean install

# Run with Docker
docker compose up -d --build gateway-api
```

## Port
Gateway API runs on port **9999**.