# Build and Run

## Full Platform Build and Run

To build and run the entire platform, execute the provided script:

```bash
./build-and-run.sh
```

This script compiles all microservices and starts them in their respective containers.

## Run a Single Service

### Run the `data-api` Service

To build and run only the `data-api` service:

```bash
docker compose up -d --build data-api
```

This command builds the `data-api` Docker image and starts the container in detached mode, making the service available on port 9995.