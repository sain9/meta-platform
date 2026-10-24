
#!/usr/bin/env bash

set -Eeuo pipefail

# ============================================================
# Keycloak Setup Script for Meta-Platform / user-access-api
#
# Features:
#   1. Authenticate to Keycloak Admin API
#   2. Create the realm if it does not exist
#   3. Create or update the OIDC client
#   4. Configure client authentication and flows
#   5. Enable service accounts
#   6. Assign realm-management roles to the service account
#   7. Retrieve and display the client secret
#   8. Test the client_credentials grant
#   9. Save logs with restricted permissions
#
# Requirements: bash, curl, jq
# ============================================================

# -------------------- Configuration -------------------------

KEYCLOAK_HOST="${KEYCLOAK_HOST:-http://localhost:9001}"

ADMIN_REALM="${ADMIN_REALM:-master}"
ADMIN_USERNAME="admin"
ADMIN_PASSWORD="admin123"

REALM_NAME="${REALM_NAME:-platform}"

CLIENT_ID="${CLIENT_ID:-platform-gateway}"
CLIENT_NAME="${CLIENT_NAME:-platform-gateway}"

# Requested realm roles to create.
REALM_ROLES="${REALM_ROLES:-ADMIN,DEVELOPER}"

# Session lifetime in seconds.
SESSION_SECONDS="${SESSION_SECONDS:-3600}"

# Roles assigned to the client service account.
SERVICE_ACCOUNT_ROLES=(
    "query-users"
    "query-groups"
    "view-users"
    "view-realm"
    "create-client"
    "manage-users"
    "manage-realm"
)

KEYCLOAK_HOST="${KEYCLOAK_HOST%/}"

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="${SCRIPT_DIR}/logs"
mkdir -p "$LOG_DIR"

LOG_FILE="${LOG_DIR}/keycloak-setup-$(date +%Y%m%d-%H%M%S).log"

# Create the log file before changing its permissions.
touch "$LOG_FILE"
chmod 600 "$LOG_FILE"

# Log output to both terminal and file.
exec > >(tee -a "$LOG_FILE") 2>&1

# -------------------- Error Handling ------------------------

trap 'echo "[ERROR] Script failed at line ${LINENO}. Check: ${LOG_FILE}" >&2' ERR

# -------------------- Helpers -------------------------------

API_BODY=""
API_STATUS=""
ADMIN_TOKEN=""
CLIENT_INTERNAL_ID=""
REALM_MGMT_CLIENT_ID=""
SERVICE_ACCOUNT_USER_ID=""
CLIENT_SECRET=""

log() {
    printf '[INFO] %s\n' "$*"
}

success() {
    printf '[SUCCESS] %s\n' "$*"
}

warn() {
    printf '[WARNING] %s\n' "$*" >&2
}

die() {
    printf '[ERROR] %s\n' "$*" >&2
    exit 1
}

check_dependencies() {
    local dependency

    for dependency in curl jq; do
        command -v "$dependency" >/dev/null 2>&1 ||
            die "Required command not found: ${dependency}"
    done
}

# Make an authenticated Admin REST API request.
#
# Usage:
#   api_request METHOD URL [JSON_BODY]
#
# Results:
#   API_STATUS = HTTP status code
#   API_BODY   = response body
#
# This function deliberately does not fail on HTTP 404,
# allowing callers to check whether resources exist.
api_request() {
    local method="$1"
    local url="$2"
    local payload="${3:-}"
    local response

    local -a curl_args=(
        --silent
        --show-error
        --request "$method"
        --url "$url"
        --header "Authorization: Bearer ${ADMIN_TOKEN}"
        --header "Accept: application/json"
        --write-out $'\n%{http_code}'
    )

    if [[ -n "$payload" ]]; then
        curl_args+=(
            --header "Content-Type: application/json"
            --data "$payload"
        )
    fi

    response="$(curl "${curl_args[@]}")"

    API_STATUS="${response##*$'\n'}"
    API_BODY="${response%$'\n'*}"
}

# Require an HTTP 2xx response from the previous API request.
require_success() {
    local action="$1"

    if [[ ! "$API_STATUS" =~ ^2[0-9][0-9]$ ]]; then
        printf '[ERROR] %s failed (HTTP %s)\n' \
            "$action" "$API_STATUS" >&2

        if [[ -n "$API_BODY" ]]; then
            printf '%s\n' "$API_BODY" >&2
        fi

        exit 1
    fi
}

# -------------------- Admin Authentication ------------------

authenticate_admin() {
    local admin_password
    local token_response
    local token_status
    local token_body

    log "Authenticating with Keycloak admin realm: ${ADMIN_REALM}"

    # Use the configured admin password without prompting interactively.
    admin_password="admin123"

    token_response="$(
        curl --silent --show-error \
            --request POST \
            --url "${KEYCLOAK_HOST}/realms/${ADMIN_REALM}/protocol/openid-connect/token" \
            --header "Content-Type: application/x-www-form-urlencoded" \
            --data-urlencode "grant_type=password" \
            --data-urlencode "client_id=admin-cli" \
            --data-urlencode "username=${ADMIN_USERNAME}" \
            --data-urlencode "password=${admin_password}" \
            --write-out $'\n%{http_code}'
    )"

    # Do not retain the admin password longer than necessary.
    unset admin_password

    token_status="${token_response##*$'\n'}"
    token_body="${token_response%$'\n'*}"

    if [[ ! "$token_status" =~ ^2[0-9][0-9]$ ]]; then
        printf '[ERROR] Admin authentication failed (HTTP %s)\n' \
            "$token_status" >&2
        printf '%s\n' "$token_body" >&2
        die "Check admin credentials and ensure admin-cli supports password grants."
    fi

    ADMIN_TOKEN="$(jq -er '.access_token' <<< "$token_body")"

    success "Admin authentication successful."
}

# -------------------- Create Realm --------------------------

create_realm_if_missing() {
    log "Checking realm: ${REALM_NAME}"

    api_request GET \
        "${KEYCLOAK_HOST}/admin/realms/${REALM_NAME}"

    case "$API_STATUS" in
        200)
            log "Realm already exists. Updating session settings."

            local realm_payload
            realm_payload="$(
                jq -n \
                    --arg realm "$REALM_NAME" \
                    --argjson session "$SESSION_SECONDS" \
                    '{
                        realm: $realm,
                        enabled: true,
                        ssoSessionMaxLifespan: $session,
                        ssoSessionIdleTimeout: $session
                    }'
            )"

            api_request PUT \
                "${KEYCLOAK_HOST}/admin/realms/${REALM_NAME}" \
                "$realm_payload"

            require_success "Update realm settings"
            ;;
        404)
            log "Realm does not exist. Creating it."

            local realm_payload
            realm_payload="$(
                jq -n \
                    --arg realm "$REALM_NAME" \
                    --argjson session "$SESSION_SECONDS" \
                    '{
                        realm: $realm,
                        enabled: true,
                        ssoSessionMaxLifespan: $session,
                        ssoSessionIdleTimeout: $session
                    }'
            )"

            api_request POST \
                "${KEYCLOAK_HOST}/admin/realms" \
                "$realm_payload"

            require_success "Create realm"
            ;;
        *)
            require_success "Check realm"
            ;;
    esac

    success "Realm ${REALM_NAME} is ready."
}

# -------------------- Create Realm Roles --------------------

create_realm_roles() {
    local role
    local role_payload

    IFS=',' read -r -a roles <<< "$REALM_ROLES"

    for role in "${roles[@]}"; do
        # Trim leading/trailing whitespace.
        role="$(printf '%s' "$role" | xargs)"

        [[ -n "$role" ]] || continue

        log "Checking realm role: ${role}"

        api_request GET \
            "${KEYCLOAK_HOST}/admin/realms/${REALM_NAME}/roles/${role}"

        case "$API_STATUS" in
            200)
                log "Realm role already exists: ${role}"
                ;;
            404)
                role_payload="$(
                    jq -n \
                        --arg name "$role" \
                        '{name: $name}'
                )"

                api_request POST \
                    "${KEYCLOAK_HOST}/admin/realms/${REALM_NAME}/roles" \
                    "$role_payload"

                require_success "Create realm role ${role}"
                success "Created realm role: ${role}"
                ;;
            *)
                require_success "Check realm role ${role}"
                ;;
        esac
    done
}

# -------------------- Create or Update Client ---------------

configure_client() {
    local client_list
    local client_payload
    local client_url

    log "Checking OIDC client: ${CLIENT_ID}"

    api_request GET \
        "${KEYCLOAK_HOST}/admin/realms/${REALM_NAME}/clients?clientId=${CLIENT_ID}"

    require_success "Find client"

    client_list="$API_BODY"

    CLIENT_INTERNAL_ID="$(
        jq -r --arg client_id "$CLIENT_ID" \
            '[.[] | select(.clientId == $client_id)][0].id // empty' \
            <<< "$client_list"
    )"

    if [[ -z "$CLIENT_INTERNAL_ID" ]]; then
        log "Client does not exist. Creating it."

        client_payload="$(
            jq -n \
                --arg client_id "$CLIENT_ID" \
                --arg name "$CLIENT_NAME" \
                '{
                    clientId: $client_id,
                    name: $name,
                    enabled: true,
                    protocol: "openid-connect",
                    clientAuthenticatorType: "client-secret",
                    publicClient: false,
                    bearerOnly: false,
                    authorizationServicesEnabled: false,
                    standardFlowEnabled: true,
                    directAccessGrantsEnabled: true,
                    implicitFlowEnabled: false,
                    serviceAccountsEnabled: true,
                    consentRequired: false
                }'
        )"

        api_request POST \
            "${KEYCLOAK_HOST}/admin/realms/${REALM_NAME}/clients" \
            "$client_payload"

        require_success "Create OIDC client"

        # Look up the internal client ID after creation.
        api_request GET \
            "${KEYCLOAK_HOST}/admin/realms/${REALM_NAME}/clients?clientId=${CLIENT_ID}"

        require_success "Retrieve newly created client"

        CLIENT_INTERNAL_ID="$(
            jq -er --arg client_id "$CLIENT_ID" \
                '[.[] | select(.clientId == $client_id)][0].id' \
                <<< "$API_BODY"
        )"

        success "Created client: ${CLIENT_ID}"
    else
        log "Client already exists. Updating requested settings."

        client_url="${KEYCLOAK_HOST}/admin/realms/${REALM_NAME}/clients/${CLIENT_INTERNAL_ID}"

        # Preserve other existing client configuration while
        # applying the required settings.
        api_request GET "$client_url"
        require_success "Retrieve existing client"

        client_payload="$(
            jq \
                --arg name "$CLIENT_NAME" \
                '
                .name = $name
                | .enabled = true
                | .protocol = "openid-connect"
                | .clientAuthenticatorType = "client-secret"
                | .publicClient = false
                | .bearerOnly = false
                | .authorizationServicesEnabled = false
                | .standardFlowEnabled = true
                | .directAccessGrantsEnabled = true
                | .implicitFlowEnabled = false
                | .serviceAccountsEnabled = true
                ' <<< "$API_BODY"
        )"

        api_request PUT "$client_url" "$client_payload"
        require_success "Update OIDC client"

        success "Updated client: ${CLIENT_ID}"
    fi

    success "Client configuration completed."
}

# -------------------- Retrieve Client Secret ----------------

retrieve_client_secret() {
    log "Retrieving client secret."

    api_request GET \
        "${KEYCLOAK_HOST}/admin/realms/${REALM_NAME}/clients/${CLIENT_INTERNAL_ID}/client-secret"

    require_success "Retrieve client secret"

    CLIENT_SECRET="$(jq -er '.value' <<< "$API_BODY")"

    [[ -n "$CLIENT_SECRET" ]] ||
        die "Keycloak returned an empty client secret."

    success "Client secret retrieved."
}

# -------------------- Locate Service Account ----------------

get_service_account_user() {
    log "Looking up client service-account user."

    api_request GET \
        "${KEYCLOAK_HOST}/admin/realms/${REALM_NAME}/clients/${CLIENT_INTERNAL_ID}/service-account-user"

    require_success "Retrieve service-account user"

    SERVICE_ACCOUNT_USER_ID="$(jq -er '.id' <<< "$API_BODY")"

    success "Service-account user found."
}

# -------------------- Assign Service Account Roles -----------

assign_service_account_roles() {
    local role
    local role_response
    local role_id
    local role_payload
    local role_url

    log "Finding realm-management client."

    api_request GET \
        "${KEYCLOAK_HOST}/admin/realms/${REALM_NAME}/clients?clientId=realm-management"

    require_success "Find realm-management client"

    REALM_MGMT_CLIENT_ID="$(
        jq -er \
            '[.[] | select(.clientId == "realm-management")][0].id' \
            <<< "$API_BODY"
    )"

    for role in "${SERVICE_ACCOUNT_ROLES[@]}"; do
        log "Assigning service-account role: ${role}"

        role_url="${KEYCLOAK_HOST}/admin/realms/${REALM_NAME}/clients/${REALM_MGMT_CLIENT_ID}/roles/${role}"

        api_request GET "$role_url"

        if [[ "$API_STATUS" != "200" ]]; then
            printf '[ERROR] Could not find realm-management role: %s\n' \
                "$role" >&2
            printf '%s\n' "$API_BODY" >&2
            die "Verify that the role exists in this Keycloak version."
        fi

        role_response="$API_BODY"
        role_id="$(jq -er '.id' <<< "$role_response")"

        role_payload="$(
            jq -n \
                --arg id "$role_id" \
                --arg name "$role" \
                '[{id: $id, name: $name}]'
        )"

        # Keycloak role-mapping POST is safe to repeat for an
        # already-assigned role in normal Keycloak configurations.
        api_request POST \
            "${KEYCLOAK_HOST}/admin/realms/${REALM_NAME}/users/${SERVICE_ACCOUNT_USER_ID}/role-mappings/clients/${REALM_MGMT_CLIENT_ID}" \
            "$role_payload"

        require_success "Assign service-account role ${role}"

        success "Assigned: ${role}"
    done

    success "All requested service-account roles processed."
}

# -------------------- Verify Client Credentials --------------

test_client_credentials() {
    local test_response
    local test_status
    local test_body

    log "Testing client_credentials grant."

    test_response="$(
        curl --silent --show-error \
            --request POST \
            --url "${KEYCLOAK_HOST}/realms/${REALM_NAME}/protocol/openid-connect/token" \
            --header "Content-Type: application/x-www-form-urlencoded" \
            --data-urlencode "grant_type=client_credentials" \
            --data-urlencode "client_id=${CLIENT_ID}" \
            --data-urlencode "client_secret=${CLIENT_SECRET}" \
            --write-out $'\n%{http_code}'
    )"

    test_status="${test_response##*$'\n'}"
    test_body="${test_response%$'\n'*}"

    if [[ ! "$test_status" =~ ^2[0-9][0-9]$ ]]; then
        printf '[ERROR] Client credentials test failed (HTTP %s)\n' \
            "$test_status" >&2
        printf '%s\n' "$test_body" >&2
        die "Check client configuration and service-account settings."
    fi

    if ! jq -e '.access_token | type == "string" and length > 0' \
        >/dev/null <<< "$test_body"; then
        die "Token endpoint response did not contain an access token."
    fi

    success "Client credentials grant works."
}

# -------------------- Final Output --------------------------

print_summary() {
    printf '\n'
    printf '%s\n' "============================================================"
    printf '%s\n' " KEYCLOAK SETUP COMPLETED"
    printf '%s\n' "============================================================"
    printf 'Keycloak URL : %s\n' "$KEYCLOAK_HOST"
    printf 'Realm        : %s\n' "$REALM_NAME"
    printf 'Client ID    : %s\n' "$CLIENT_ID"
    printf 'Client UUID  : %s\n' "$CLIENT_INTERNAL_ID"
    printf 'Session      : %s seconds\n' "$SESSION_SECONDS"
    printf 'Log file     : %s\n' "$LOG_FILE"

    printf '\n%s\n' "Client settings:"
    printf '%s\n' "  Client authentication : ON"
    printf '%s\n' "  Authorization         : OFF"
    printf '%s\n' "  Standard flow         : ON"
    printf '%s\n' "  Direct access grants  : ON"
    printf '%s\n' "  Service accounts      : ON"

    printf '\n%s\n' "Assigned service-account roles:"
    printf '  - %s\n' "${SERVICE_ACCOUNT_ROLES[@]}"

    printf '\n%s\n' "Client secret (sensitive):"
    printf '%s\n' "$CLIENT_SECRET"

    printf '\n%s\n' "Use these values in user-access-api application.properties:"
    printf 'keycloak.server-url=%s\n' "$KEYCLOAK_HOST"
    printf 'keycloak.realm=%s\n' "$REALM_NAME"
    printf 'keycloak.client-id=%s\n' "$CLIENT_ID"
    printf 'keycloak.client-secret=%s\n' "$CLIENT_SECRET"

    printf '\n%s\n' "IMPORTANT:"
    printf '%s\n' "- Confirm property names match your application's configuration."
    printf '%s\n' "- Keep the client secret out of Git and shared logs."
    printf '%s\n' "- Use HTTPS and a least-privilege service account in production."
    printf '%s\n' "============================================================"
}

# -------------------- Main ----------------------------------

main() {
    check_dependencies

    log "Starting Keycloak setup."
    log "Keycloak host: ${KEYCLOAK_HOST}"
    log "Realm: ${REALM_NAME}"
    log "Client: ${CLIENT_ID}"

    authenticate_admin
    create_realm_if_missing
    create_realm_roles
    configure_client
    retrieve_client_secret
    get_service_account_user
    assign_service_account_roles
    test_client_credentials
    print_summary

    # Do not leave the admin token in the shell environment.
    unset ADMIN_TOKEN

    success "Setup finished successfully."
}

main "$@"
