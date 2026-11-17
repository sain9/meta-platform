# Keycloak Test Commands

Use these `curl` commands to verify your Keycloak setup after the container is running.

---

## 1. Test Client Credentials Flow

If you've created the realm and client (`platform-gateway`), copy the secret and test it:

```bash
curl -X POST "http://localhost:9001/realms/platform/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=platform-gateway" \
  -d "client_secret=xyzabc"
```

**Expected:** A JSON response with an `access_token`.

---

## 2. Test User Password Grant

After registering a user (e.g., username `hussain`, password `hussain`), acquire a token for that user:

```bash
curl -X POST "http://localhost:9001/realms/platform/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=platform-gateway" \
  -d "client_secret=xyzabc" \
  -d "username=hussain" \
  -d "password=hussain"
```

**Expected:** A JSON response with an `access_token` for the user.

---

## 3. Access Protected Resources

Use the obtained `access_token` to access protected resources:

```bash
curl "http://localhost:8081/api/data/health" \
  -H "Authorization: Bearer <access_token>"
```

Replace `<access_token>` with the token from the previous responses.