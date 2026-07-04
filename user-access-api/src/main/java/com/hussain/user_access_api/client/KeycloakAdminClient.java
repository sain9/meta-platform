package com.hussain.user_access_api.client;

import com.hussain.user_access_api.config.KeycloakProperties;
import com.hussain.user_access_api.dto.keycloak.request.CreateUserRequest;
import com.hussain.user_access_api.dto.keycloak.request.CredentialRepresentation;
import com.hussain.user_access_api.dto.keycloak.response.RoleRepresentation;
import com.hussain.user_access_api.dto.keycloak.response.UserRepresentation;
import com.hussain.user_access_api.dto.request.UpdateUserRequest;
import com.hussain.user_access_api.exception.KeycloakException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KeycloakAdminClient {

    private final RestClient restClient;

    private final KeycloakProperties keycloakProperties;

    /**
     * Builds Keycloak Admin REST endpoint.
     */
    private String adminUrl(String path) {

        return "%s/admin/realms/%s%s"
                .formatted(
                        keycloakProperties.getServerUrl(),
                        keycloakProperties.getRealm(),
                        path);

    }

    /**
     * Creates a user in Keycloak.
     *
     * @return Created User ID
     */
    public String createUser(
            String accessToken,
            CreateUserRequest request) {

        try {

            ResponseEntity<Void> response = restClient.post()

                    .uri(adminUrl("/users"))

                    .header(HttpHeaders.AUTHORIZATION,
                            "Bearer " + accessToken)

                    .contentType(MediaType.APPLICATION_JSON)

                    .body(request)

                    .retrieve()

                    .toBodilessEntity();

            URI location = response.getHeaders().getLocation();

            if (location == null) {
                throw new KeycloakException(
                        "User created but Location header is missing.");
            }

            String path = location.getPath();

            return path.substring(path.lastIndexOf('/') + 1);

        } catch (RestClientException ex) {

            throw new KeycloakException(
                    "Unable to create user in Keycloak.",
                    ex);

        }

    }

    /**
     * Finds a user by username.
     */
    public UserRepresentation findUserByUsername(
            String accessToken,
            String username) {

        try {

            String url = adminUrl("/users")
                    + "?username=" + username
                    + "&exact=true";

            List<UserRepresentation> users =
                    restClient.get()

                            .uri(url)

                            .header(HttpHeaders.AUTHORIZATION,
                                    "Bearer " + accessToken)

                            .retrieve()

                            .body(new ParameterizedTypeReference<List<UserRepresentation>>() {});

            if (users == null || users.isEmpty()) {
                return null;
            }

            return users.get(0);

        } catch (RestClientException ex) {

            throw new KeycloakException(
                    "Unable to find user in Keycloak.",
                    ex);

        }

    }

    /**
     * Returns the Keycloak User ID for a username.
     */
    public String getUserIdByUsername(
            String accessToken,
            String username) {

        UserRepresentation user =
                findUserByUsername(
                        accessToken,
                        username);

        if (user == null) {

            throw new KeycloakException(
                    "User not found: " + username);

        }

        return user.getId();

    }

    /**
     * Retrieves a realm role.
     */
    public RoleRepresentation getRealmRole(
            String accessToken,
            String roleName) {

        try {

            return restClient.get()

                    .uri(adminUrl("/roles/" + roleName))

                    .header(HttpHeaders.AUTHORIZATION,
                            "Bearer " + accessToken)

                    .retrieve()

                    .body(RoleRepresentation.class);

        } catch (RestClientException ex) {

            throw new KeycloakException(
                    "Unable to fetch realm role: " + roleName,
                    ex);

        }

    }

    /**
     * Assigns a realm role to a user.
     */
    public void assignRealmRole(
            String accessToken,
            String userId,
            RoleRepresentation role) {

        try {

            restClient.post()

                    .uri(adminUrl("/users/"
                            + userId
                            + "/role-mappings/realm"))

                    .header(HttpHeaders.AUTHORIZATION,
                            "Bearer " + accessToken)

                    .contentType(MediaType.APPLICATION_JSON)

                    .body(List.of(role))

                    .retrieve()

                    .toBodilessEntity();

        } catch (RestClientException ex) {

            throw new KeycloakException(
                    "Unable to assign realm role.",
                    ex);

        }

    }

    /**
     * Remove Realm Role
     */
    public void removeRealmRole(
            String accessToken,
            String userId,
            RoleRepresentation role) {

        try {

            restClient.method(HttpMethod.DELETE)

                    .uri(adminUrl("/users/"
                            + userId
                            + "/role-mappings/realm"))

                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + accessToken)

                    .contentType(MediaType.APPLICATION_JSON)

                    .body(List.of(role))

                    .retrieve()

                    .toBodilessEntity();

        } catch (RestClientException ex) {

            throw new KeycloakException(
                    "Unable to remove realm role.",
                    ex);

        }

    }


    /**
     * Resets the user's password.
     */
    public void resetPassword(
            String accessToken,
            String userId,
            String newPassword) {

        CredentialRepresentation credential =
                new CredentialRepresentation();

        credential.setType("password");
        credential.setValue(newPassword);
        credential.setTemporary(false);

        try {

            restClient.put()

                    .uri(adminUrl("/users/"
                            + userId
                            + "/reset-password"))

                    .header(HttpHeaders.AUTHORIZATION,
                            "Bearer " + accessToken)

                    .contentType(MediaType.APPLICATION_JSON)

                    .body(credential)

                    .retrieve()

                    .toBodilessEntity();

        } catch (RestClientException ex) {

            throw new KeycloakException(
                    "Unable to reset user password.",
                    ex);

        }

    }

    /**
     * Delete User
     */
    public void deleteUser(
            String accessToken,
            String userId) {

        try {

            restClient.delete()

                    .uri(adminUrl("/users/" + userId))

                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + accessToken)

                    .retrieve()

                    .toBodilessEntity();

        } catch (RestClientException ex) {

            throw new KeycloakException(
                    "Unable to delete user.",
                    ex);

        }

    }

    /**
     * Retrieves all realm roles assigned to a user.
     */
    public List<RoleRepresentation> getUserRealmRoles(
            String accessToken,
            String userId) {

        try {

            return restClient.get()

                    .uri(adminUrl("/users/"
                            + userId
                            + "/role-mappings/realm"))

                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + accessToken)

                    .retrieve()

                    .body(new ParameterizedTypeReference<List<RoleRepresentation>>() {});

        } catch (RestClientException ex) {

            throw new KeycloakException(
                    "Unable to fetch user roles.",
                    ex);

        }

    }

    /**
     * Update User
     */
    public void updateUser(
            String accessToken,
            String userId,
            UpdateUserRequest request) {

        try {

            UserRepresentation user =
                    new UserRepresentation();

            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());

            restClient.put()

                    .uri(adminUrl("/users/" + userId))

                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + accessToken)

                    .contentType(MediaType.APPLICATION_JSON)

                    .body(user)

                    .retrieve()

                    .toBodilessEntity();

        } catch (RestClientException ex) {

            throw new KeycloakException(
                    "Unable to update user.",
                    ex);

        }

    }

    public void updateUserStatus(
            String accessToken,
            String userId,
            boolean enabled) {

        try {

            UserRepresentation user =
                    new UserRepresentation();

            user.setEnabled(enabled);

            restClient.put()

                    .uri(adminUrl("/users/" + userId))

                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + accessToken)

                    .contentType(MediaType.APPLICATION_JSON)

                    .body(user)

                    .retrieve()

                    .toBodilessEntity();

        } catch (RestClientException ex) {

            throw new KeycloakException(
                    "Unable to update user status.",
                    ex);

        }

    }

}