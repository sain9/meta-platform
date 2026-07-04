package com.hussain.user_access_api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserResponse {

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private List<String> roles;

}