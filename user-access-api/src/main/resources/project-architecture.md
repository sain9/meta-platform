user-access-api
│
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