package com.hussain.user_access_api.dto.keycloak.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateUserRequest {

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private boolean enabled = true;

    private List<CredentialRepresentation> credentials;

}