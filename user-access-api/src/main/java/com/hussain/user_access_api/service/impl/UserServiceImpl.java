package com.hussain.user_access_api.service.impl;

import com.hussain.user_access_api.dto.request.*;
import com.hussain.user_access_api.dto.response.LoginResponse;
import com.hussain.user_access_api.dto.response.UserResponse;
import com.hussain.user_access_api.keycloak.KeycloakService;
import com.hussain.user_access_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final KeycloakService keycloakService;

    @Override
    public UserResponse register(RegisterRequest request) {

        return keycloakService.register(request);

    }

    @Override
    public LoginResponse login(LoginRequest request) {
        return keycloakService.login(request);
    }

    @Override
    public LoginResponse refreshToken(
            RefreshTokenRequest request) {

        return keycloakService.refreshToken(request);

    }

    @Override
    public void logout(LogoutRequest request) {

        keycloakService.logout(request);

    }

    @Override
    public void changePassword(
            ChangePasswordRequest request) {

        keycloakService.changePassword(request);

    }

    @Override
    public void deleteUser(String username) {

        keycloakService.deleteUser(username);

    }

    @Override
    public UserResponse getUser(String username) {

        return keycloakService.getUser(username);

    }

    @Override
    public UserResponse updateUser(String username, UpdateUserRequest request) {

        return keycloakService.updateUser(  username, request);

    }

    @Override
    public void enableUser(String username) {

        keycloakService.enableUser(username);

    }

    @Override
    public void disableUser(String username) {

        keycloakService.disableUser(username);

    }

    @Override
    public void assignRole(String username, RoleRequest request) {

        keycloakService.assignRole(username, request);

    }

    @Override
    public void removeRole(String username, RoleRequest request) {

        keycloakService.removeRole(username, request);

    }

}