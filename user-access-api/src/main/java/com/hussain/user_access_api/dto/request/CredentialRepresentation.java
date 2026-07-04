package com.hussain.user_access_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CredentialRepresentation {

    private String type;

    private String value;

    private boolean temporary;

}