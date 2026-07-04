package com.hussain.user_access_api.service;

import com.hussain.user_access_api.dto.request.*;
import com.hussain.user_access_api.dto.response.LoginResponse;
import com.hussain.user_access_api.dto.response.UserResponse;

public interface UserService {

    UserResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    LoginResponse refreshToken(RefreshTokenRequest request);

    void logout(LogoutRequest request);

    void changePassword(ChangePasswordRequest request);

    void deleteUser(String username);

    UserResponse getUser(String username);

    UserResponse updateUser(String username, UpdateUserRequest request);

    void enableUser(String username);

    void disableUser(String username);

    void assignRole(String username, RoleRequest request);

    void removeRole(String username, RoleRequest request);

}