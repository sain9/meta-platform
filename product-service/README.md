# Product Service POC

This is a proof-of-concept microservice that demonstrates how to utilize the `data-api-client` library to perform CRUD operations over gRPC.

## Architecture
- **Language**: Java 17, Spring Boot 3.5.4
- **Data Access**: Utilizes `data-api-client` to communicate with the `data-api` microservice via gRPC.
- **Port**: 8085

## REST API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/products` | Create a new product |
| `GET` | `/api/products` | Get all products |
| `GET` | `/api/products/{id}` | Get product by ID |
| `PUT` | `/api/products/{id}` | Update product by ID |
| `DELETE` | `/api/products/{id}` | Delete product by ID |

## How to Run

Ensure the `data-api` microservice is running (it is configured to look for gRPC on `localhost:9003`).

```bash
mvn spring-boot:run -pl product-service
```

## Example Requests

### Create a Product
```bash
curl -X POST http://localhost:8085/api/products \
  -H "Content-Type: application/json" \
  -d '{"id": 2, "name": "REST-Product-2", "price": 99.99}'
```

### Update a Product
```bash
curl -X PUT http://localhost:8085/api/products/2 \
  -H "Content-Type: application/json" \
  -d '{"name": "Updated-REST-Product", "price": 75.00}'
```

## Dependencies
- `data-api-client`: For gRPC communication with `data-api`.
- `spring-boot-starter-web`: For REST API functionality.
