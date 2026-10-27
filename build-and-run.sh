#!/usr/bin/env bash

set -Eeuo pipefail

# Navigate to the repository root
cd "$(dirname "$0")"

echo "========================================"
echo "Stopping existing containers..."
echo "========================================"
docker compose down

echo
echo "========================================"
echo "Removing dangling Docker build cache..."
echo "========================================"
docker builder prune -f

echo
echo "========================================"
echo "Cleaning and installing all Maven projects..."
echo "========================================"

# Clean and install the root project and all six modules.
# Maven automatically resolves dependency order.
mvn -B clean install -DskipTests

echo
echo "========================================"
echo "Building Docker images without cache..."
echo "========================================"

docker compose build --no-cache

echo
echo "========================================"
echo "Starting Meta Platform..."
echo "========================================"

docker compose up -d

echo
echo "========================================"
echo "Running containers..."
echo "========================================"

docker compose ps

echo
echo "========================================"
echo "Build and deployment completed!"
echo "========================================"