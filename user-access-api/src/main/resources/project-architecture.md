# User Access API - Project Architecture

```
user-access-api/
├── pom.xml
│
├── src
│   └── main
│       ├── java
│       │   └── com
│       │       └── hussain
│       │           └── useraccess
│       │
│       │               ├── UserAccessApplication.java
│       │               │
│       │               ├── config
│       │               │      ├── RestClientConfig.java
│       │               │      └── SecurityConfig.java
│       │               │
│       │               ├── controller
│       │               │      └── UserController.java
│       │               │
│       │               ├── service
│       │               │      ├── UserService.java
│       │               │      └── impl
│       │               │             └── UserServiceImpl.java
│       │               │
│       │               ├── keycloak
│       │               │      ├── KeycloakAdminService.java
│       │               │      └── KeycloakTokenService.java
│       │               │
│       │               ├── client
│       │               │      ├── KeycloakAdminClient.java
│       │               │      └── KeycloakTokenClient.java
│       │               │
│       │               ├── dto
│       │               │      ├── request
│       │               │      │      ├── LoginRequest.java
│       │               │      │      └── RegisterRequest.java
│       │               │      │
│       │               │      └── response
│       │               │             ├── ApiResponse.java
│       │               │             ├── LoginResponse.java
│       │               │             └── UserResponse.java
│       │               │
│       │               ├── enums
│       │               │      └── RealmRole.java
│       │               │
│       │               ├── exception
│       │               │      ├── GlobalExceptionHandler.java
│       │               │      ├── InvalidRoleException.java
│       │               │      ├── KeycloakException.java
│       │               │      └── UserAlreadyExistsException.java
│       │               │
│       │               ├── constants
│       │               │      └── ApiConstants.java
│       │               │
│       │               └── util
│       │                      └── JwtUtil.java
│       │
│       └── resources
│              └── application.properties
│
└── README.md
```

## Component Overview

| Layer | Classes | Purpose |
|-------|---------|---------|
| **Config** | `RestClientConfig`, `SecurityConfig` | HTTP client setup, Spring Security & Keycloak integration |
| **Controller** | `UserController` | REST endpoints for user lifecycle management |
| **Service** | `UserService`, `UserServiceImpl` | Business logic for user operations |
| **Keycloak** | `KeycloakAdminService`, `KeycloakTokenService` | Orchestration layer for Keycloak interactions |
| **Client** | `KeycloakAdminClient`, `KeycloakTokenClient` | Low-level HTTP clients calling Keycloak Admin & Token APIs |
| **DTO** | Request/Response classes | Data contracts for API inputs/outputs |
| **Enums** | `RealmRole` | Centralized role definitions (ADMIN, DEVELOPER, USER, VIEWER) |
| **Exception** | Custom exceptions + Global handler | Consistent error responses |
| **Util** | `JwtUtil` | JWT parsing/validation helpers |

## Build & Run

```bash
# From project root
mvn clean install

# Run with Docker
docker compose up -d --build user-access-api
```

## Port
User Access API runs on port **9997**.