# Meta Platform: Enterprise AI-driven Metadata Platform

This repository is the parent workspace for all platform components, including:

*   **Gateway API:** Runs on port `9999`
*   **User Access API:** Runs on port `9997`
*   **Data API:** Runs on port `9995`
*   **Definition API:** Runs on port `9996`

## Future Services
The platform is designed to scale to include:
*   AI Data Service
*   AI Definition Service
*   Notification API
*   Workflow API
*   Audit API

## Repository Structure
This monorepo organizes services by their primary function for clear separation of concerns.

## Getting Started
1.  Use `./build-and-run.sh` for a full platform startup.
2.  Use `docker compose up -d --build <service-name>` to run individual services (e.g., `data-api`).

**Note:** All services communicate via their designated internal ports. Check the specific service documentation for operational details.