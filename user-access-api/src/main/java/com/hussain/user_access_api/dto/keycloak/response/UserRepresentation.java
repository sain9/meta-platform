package com.hussain.user_access_api.dto.keycloak.response;

import lombok.Data;

@Data
public class UserRepresentation {

    private String id;

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private boolean enabled;

}