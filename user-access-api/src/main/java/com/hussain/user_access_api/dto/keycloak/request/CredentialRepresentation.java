package com.hussain.user_access_api.dto.keycloak.request;

import lombok.Data;

@Data
public class CredentialRepresentation {

    private String type;

    private String value;

    private boolean temporary;

}