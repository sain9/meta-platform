package com.hussain.user_access_api.keycloak;

import com.hussain.user_access_api.client.KeycloakAdminClient;
import com.hussain.user_access_api.client.KeycloakTokenClient;
import com.hussain.user_access_api.dto.keycloak.request.CreateUserRequest;
import com.hussain.user_access_api.dto.keycloak.response.RoleRepresentation;
import com.hussain.user_access_api.dto.keycloak.response.TokenResponse;
import com.hussain.user_access_api.dto.keycloak.response.UserRepresentation;
import com.hussain.user_access_api.dto.request.*;
import com.hussain.user_access_api.dto.response.LoginResponse;
import com.hussain.user_access_api.dto.response.UserResponse;
import com.hussain.user_access_api.exception.KeycloakException;
import com.hussain.user_access_api.mapper.KeycloakMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final KeycloakTokenClient keycloakTokenClient;

    private final KeycloakAdminClient keycloakAdminClient;

    private final KeycloakMapper keycloakMapper;

    public UserResponse register(RegisterRequest request) {

        /*
         * Step 1 - Obtain Service Account Token
         */
        TokenResponse tokenResponse =
                keycloakTokenClient.getServiceAccountToken();

        String accessToken =
                tokenResponse.getAccessToken();

        /*
         * Step 2 - Convert Request
         */
        CreateUserRequest createUserRequest =
                keycloakMapper.toCreateUserRequest(request);

        /*
         * Step 3 - Create User
         */
        String userId =
                keycloakAdminClient.createUser(
                        accessToken,
                        createUserRequest);

        /*
         * Step 4 - Fetch Role
         */
        RoleRepresentation role =
                keycloakAdminClient.getRealmRole(
                        accessToken,
                        request.getRole().name());

        /*
         * Step 5 - Assign Role
         */
        keycloakAdminClient.assignRealmRole(
                accessToken,
                userId,
                role);

        /*
         * Step 6 - Return Response
         */
        return keycloakMapper.toUserResponse(request);

    }

    public LoginResponse login(LoginRequest request) {

        TokenResponse tokenResponse =
                keycloakTokenClient.login(
                        request.getUsername(),
                        request.getPassword());

        return keycloakMapper.toLoginResponse(tokenResponse);

    }

    public LoginResponse refreshToken(
            RefreshTokenRequest request) {

        TokenResponse tokenResponse =
                keycloakTokenClient.refreshToken(
                        request.getRefreshToken());

        return keycloakMapper.toLoginResponse(
                tokenResponse);

    }

    public void logout(LogoutRequest request) {

        keycloakTokenClient.logout(
                request.getRefreshToken());

    }

    /**
     * Change Password
     */
    public void changePassword(
            ChangePasswordRequest request) {

        /*
         * Step 1 - Verify Current Password
         *
         * If the current password is incorrect,
         * Keycloak will reject the login request.
         */
        keycloakTokenClient.login(
                request.getUsername(),
                request.getCurrentPassword());

        /*
         * Step 2 - Obtain Service Account Token
         */
        TokenResponse tokenResponse =
                keycloakTokenClient.getServiceAccountToken();

        String accessToken =
                tokenResponse.getAccessToken();

        /*
         * Step 3 - Retrieve User Id
         */
        String userId =
                keycloakAdminClient.getUserIdByUsername(
                        accessToken,
                        request.getUsername());

        /*
         * Step 4 - Reset Password
         */
        keycloakAdminClient.resetPassword(
                accessToken,
                userId,
                request.getNewPassword());

    }

    /**
     * Delete User
     */
    public void deleteUser(String username) {

        /*
         * Step 1 - Obtain Service Account Token
         */
        TokenResponse tokenResponse =
                keycloakTokenClient.getServiceAccountToken();

        String accessToken =
                tokenResponse.getAccessToken();

        /*
         * Step 2 - Find User Id
         */
        String userId =
                keycloakAdminClient.getUserIdByUsername(
                        accessToken,
                        username);

        /*
         * Step 3 - Delete User
         */
        keycloakAdminClient.deleteUser(
                accessToken,
                userId);

    }

    /**
     * Get User
     */
    public UserResponse getUser(String username) {

        /*
         * Step 1 - Service Account Token
         */
        TokenResponse tokenResponse =
                keycloakTokenClient.getServiceAccountToken();

        String accessToken =
                tokenResponse.getAccessToken();

        /*
         * Step 2 - Find User
         */
        UserRepresentation user =
                keycloakAdminClient.findUserByUsername(
                        accessToken,
                        username);

        if (user == null) {

            throw new KeycloakException(
                    "User not found: " + username);

        }

        /*
         * Step 3 - Get Roles
         */
        List<RoleRepresentation> roles =
                keycloakAdminClient.getUserRealmRoles(
                        accessToken,
                        user.getId());

        /*
         * Step 4 - Build Response
         */
        return UserResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(
                        roles.stream()
                                .map(RoleRepresentation::getName)
                                .toList())
                .build();

    }

    /**
     * Update User
     */
    public UserResponse updateUser(
            String username,
            UpdateUserRequest request) {

        /*
         * Step 1 - Service Account Token
         */
        TokenResponse tokenResponse =
                keycloakTokenClient.getServiceAccountToken();

        String accessToken =
                tokenResponse.getAccessToken();

        /*
         * Step 2 - User Id
         */
        String userId =
                keycloakAdminClient.getUserIdByUsername(
                        accessToken,
                        username);

        /*
         * Step 3 - Update
         */
        keycloakAdminClient.updateUser(
                accessToken,
                userId,
                request);

        /*
         * Step 4 - Return Updated User
         */
        return getUser(username);

    }

    public void enableUser(String username) {

        updateUserStatus(username, true);

    }

    public void disableUser(String username) {

        updateUserStatus(username, false);

    }

    private void updateUserStatus(String username, boolean enabled) {

        TokenResponse tokenResponse =
                keycloakTokenClient.getServiceAccountToken();

        String accessToken =
                tokenResponse.getAccessToken();

        String userId =
                keycloakAdminClient.getUserIdByUsername(
                        accessToken,
                        username);

        keycloakAdminClient.updateUserStatus(
                accessToken,
                userId,
                enabled);

    }

    public void assignRole(String username, RoleRequest request) {

        TokenResponse tokenResponse =
                keycloakTokenClient.getServiceAccountToken();

        String accessToken =
                tokenResponse.getAccessToken();

        String userId =
                keycloakAdminClient.getUserIdByUsername(
                        accessToken,
                        username);

        RoleRepresentation role =
                keycloakAdminClient.getRealmRole(
                        accessToken,
                        request.getRole());

        keycloakAdminClient.assignRealmRole(
                accessToken,
                userId,
                role);

    }

    public void removeRole(
            String username,
            RoleRequest request) {

        TokenResponse tokenResponse =
                keycloakTokenClient.getServiceAccountToken();

        String accessToken =
                tokenResponse.getAccessToken();

        String userId =
                keycloakAdminClient.getUserIdByUsername(
                        accessToken,
                        username);

        RoleRepresentation role =
                keycloakAdminClient.getRealmRole(
                        accessToken,
                        request.getRole());

        keycloakAdminClient.removeRealmRole(
                accessToken,
                userId,
                role);

    }


}