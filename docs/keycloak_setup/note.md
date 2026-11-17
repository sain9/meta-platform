# Keycloak Setup Guide

## Initial Setup

### Step 1: Start Keycloak Container
```bash
cd docs/keycloak_setup/docker
docker compose up -d
```

### Step 2: Generate Client Credentials
```bash
cd docs/keycloak_setup
./setup-keycloak.sh
# (or for macOS)
bash ./setup-keycloak.sh
```

### Step 3: Configure Client Credentials
Copy the generated `client_secret` and paste it into `user-access-api/src/main/resources/application.properties`:

```properties
# Keycloak Configuration
data-api.grpc.client-id=platform-gateway
data-api.grpc.client-secret=xyzabc
```

---

## Realm Setup

### Step 1: Create Realm
1. Login to Keycloak
2. In the upper-left corner, click the realm selector (default shows "Master")
3. Click "Create realm"
4. Enter:
   - Realm name: `platform`
   - Click "Create"

### Step 2: Create Client
1. Go to Clients → Create client
2. General Settings:
   - Client ID: `platform-gateway`
   - Name: `Platform Gateway`
   - Description: `Gateway authentication client`
3. Next: Configure settings for OpenID Connect (Client authentication: ON, Standard Flow: ON, Service Accounts: ON)

### Step 3: Login Settings
1. Set:
   - Root URL: `http://localhost:8080`
   - Home URL: `http://localhost:8080`
   - Valid Redirect URIs: `*`
   - Valid Post Logout Redirect URIs: `*`
   - Web Origins: `*`
2. **Warning:** Using `*` is only for development. Replace with specific URLs in production.

### Step 4: Create Realm Roles
Create these four roles in the Realm Roles menu:
*   **ADMIN**: Full administrative access.
*   **DEVELOPER**: Schema creation/modification, API testing (No user management).
*   **USER**: Standard application user access.
*   **VIEWER**: Read-only access for auditing/reporting.

---

### Step 5: Create User
1. Fill the user form: Username `hussain`, Email `hussain@example.com`, First name `Mohammad`, Last name `Hussain`.
2. **Crucially:** Email verified must be `OFF`.
3. Click "Create".

### Step 6: Set Password & Assign Role
1. **Set Password:** Go to Credentials → Set password. Use `hussain` (Username + Password). Ensure "Temporary" is `OFF`.
2. **Assign Role:** Go to Role mapping → Assign role. Filter by realm roles and assign the **ADMIN** role.