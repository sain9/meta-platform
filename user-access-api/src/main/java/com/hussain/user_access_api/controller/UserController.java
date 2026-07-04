package com.hussain.user_access_api.controller;

import com.hussain.user_access_api.dto.request.*;
import com.hussain.user_access_api.dto.response.ApiResponse;
import com.hussain.user_access_api.dto.response.LoginResponse;
import com.hussain.user_access_api.dto.response.UserResponse;
import com.hussain.user_access_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        UserResponse response = userService.register(request);

        ApiResponse<UserResponse> apiResponse =
                ApiResponse.<UserResponse>builder()
                        .success(true)
                        .message("User registered successfully.")
                        .data(response)
                        .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response =
                userService.login(request);

        ApiResponse<LoginResponse> apiResponse =
                ApiResponse.<LoginResponse>builder()
                        .success(true)
                        .message("Login successful.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponse);

    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        LoginResponse response =
                userService.refreshToken(request);

        ApiResponse<LoginResponse> apiResponse =
                ApiResponse.<LoginResponse>builder()
                        .success(true)
                        .message("Token refreshed successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponse);

    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request) {

        userService.logout(request);

        ApiResponse<Void> response =
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Logout successful.")
                        .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(request);

        ApiResponse<Void> response =
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Password changed successfully.")
                        .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable String username) {

        userService.deleteUser(username);

        ApiResponse<Void> response =
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("User deleted successfully.")
                        .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @PathVariable String username) {

        UserResponse response =
                userService.getUser(username);

        ApiResponse<UserResponse> apiResponse =
                ApiResponse.<UserResponse>builder()
                        .success(true)
                        .message("User retrieved successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponse);

    }

    @PutMapping("/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable String username,
            @Valid @RequestBody UpdateUserRequest request) {

        UserResponse response =
                userService.updateUser(username, request);

        ApiResponse<UserResponse> apiResponse =
                ApiResponse.<UserResponse>builder()
                        .success(true)
                        .message("User updated successfully.")
                        .data(response)
                        .build();

        return ResponseEntity.ok(apiResponse);

    }

    @PatchMapping("/{username}/enable")
    public ResponseEntity<ApiResponse<Void>> enableUser(
            @PathVariable String username) {

        userService.enableUser(username);

        ApiResponse<Void> response =
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("User enabled successfully.")
                        .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{username}/disable")
    public ResponseEntity<ApiResponse<Void>> disableUser(
            @PathVariable String username) {

        userService.disableUser(username);

        ApiResponse<Void> response =
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("User disabled successfully.")
                        .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{username}/roles")
    public ResponseEntity<ApiResponse<Void>> assignRole(
            @PathVariable String username,
            @Valid @RequestBody RoleRequest request) {

        userService.assignRole(username, request);

        ApiResponse<Void> response =
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Role assigned successfully.")
                        .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{username}/roles")
    public ResponseEntity<ApiResponse<Void>> removeRole(
            @PathVariable String username,
            @Valid @RequestBody RoleRequest request) {

        userService.removeRole(username, request);

        ApiResponse<Void> response =
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Role removed successfully.")
                        .build();

        return ResponseEntity.ok(response);
    }

}