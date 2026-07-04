package com.hussain.user_access_api.mapper;

import com.hussain.user_access_api.dto.keycloak.request.CreateUserRequest;
import com.hussain.user_access_api.dto.keycloak.request.CredentialRepresentation;
import com.hussain.user_access_api.dto.keycloak.response.TokenResponse;
import com.hussain.user_access_api.dto.request.RegisterRequest;
import com.hussain.user_access_api.dto.response.LoginResponse;
import com.hussain.user_access_api.dto.response.UserResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KeycloakMapper {

    public CreateUserRequest toCreateUserRequest(RegisterRequest request) {

        CredentialRepresentation credential =
                new CredentialRepresentation();

        credential.setType("password");
        credential.setValue(request.getPassword());
        credential.setTemporary(false);

        CreateUserRequest createUserRequest =
                new CreateUserRequest();

        createUserRequest.setUsername(request.getUsername());
        createUserRequest.setEmail(request.getEmail());
        createUserRequest.setFirstName(request.getFirstName());
        createUserRequest.setLastName(request.getLastName());
        createUserRequest.setEnabled(true);
        createUserRequest.setCredentials(List.of(credential));

        return createUserRequest;
    }

    public UserResponse toUserResponse(RegisterRequest request) {

        return UserResponse.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(List.of(request.getRole().name()))
                .build();

    }

    public LoginResponse toLoginResponse(
            TokenResponse response) {

        return LoginResponse.builder()
                .accessToken(response.getAccessToken())
                .refreshToken(response.getRefreshToken())
                .tokenType(response.getTokenType())
                .expiresIn(response.getExpiresIn())
                .refreshExpiresIn(response.getRefreshExpiresIn())
                .build();

    }

}