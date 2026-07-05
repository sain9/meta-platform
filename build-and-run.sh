#!/bin/bash

set -e

echo "========================================"
echo "Stopping existing containers..."
echo "========================================"
docker compose down

echo
echo "========================================"
echo "Removing dangling build cache..."
echo "========================================"
docker builder prune -f

echo
echo "========================================"
echo "Cleaning Maven build..."
echo "========================================"
mvn clean

echo
echo "========================================"
echo "Building all microservices..."
echo "========================================"
mvn clean install -DskipTests

echo
echo "========================================"
echo "Building Docker images..."
echo "========================================"
docker compose build --no-cache

echo
echo "========================================"
echo "Starting Meta Platform..."
echo "========================================"
docker compose up -d

echo
echo "========================================"
echo "Done!"
echo "========================================"

docker ps
