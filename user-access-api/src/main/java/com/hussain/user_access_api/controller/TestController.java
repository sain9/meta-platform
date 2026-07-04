//package com.hussain.user_access_api.controller;
//
//import com.hussain.user_access_api.keycloak.KeycloakAdminService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequiredArgsConstructor
//public class TestController {
//
//    private final KeycloakAdminService keycloakAdminService;
//
//    @GetMapping("/test/token")
//    public String token() {
//        return keycloakAdminService.getAccessToken();
//    }
//
//}