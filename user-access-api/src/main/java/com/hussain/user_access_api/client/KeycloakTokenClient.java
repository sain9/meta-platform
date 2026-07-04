package com.hussain.user_access_api.client;

import com.hussain.user_access_api.config.KeycloakProperties;
import com.hussain.user_access_api.dto.keycloak.response.TokenResponse;
import com.hussain.user_access_api.exception.KeycloakException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakTokenClient {

    private final RestClient restClient;
    private final KeycloakProperties keycloakProperties;

    public TokenResponse getServiceAccountToken() {

        log.info("Requesting Service Account Token...");

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();

        form.add("grant_type", "client_credentials"); //for register/or/signin
        form.add("client_id", keycloakProperties.getClientId());
        form.add("client_secret", keycloakProperties.getClientSecret());

        try {

            TokenResponse response = restClient.post()

                    .uri(keycloakProperties.getServerUrl()
                            + "/realms/"
                            + keycloakProperties.getRealm()
                            + "/protocol/openid-connect/token")

                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)

                    .body(form)

                    .retrieve()

                    .body(TokenResponse.class);

            log.info("Successfully obtained service account token.");

            return response;

        } catch (Exception ex) {

            log.error("Failed to obtain token", ex);

            throw new KeycloakException(
                    "Unable to obtain service account token.",
                    ex);

        }

    }

    public TokenResponse login(
            String username,
            String password) {

        MultiValueMap<String, String> form =
                new LinkedMultiValueMap<>();

        form.add("grant_type", "password"); //for login
        form.add("client_id", keycloakProperties.getClientId());
        form.add("client_secret", keycloakProperties.getClientSecret());

        form.add("username", username);
        form.add("password", password);

        try {

            return restClient.post()

                    .uri(keycloakProperties.getServerUrl()
                            + "/realms/"
                            + keycloakProperties.getRealm()
                            + "/protocol/openid-connect/token")

                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)

                    .body(form)

                    .retrieve()

                    .body(TokenResponse.class);

        } catch (RestClientException ex) {

            throw new KeycloakException(
                    "Invalid username or password.",
                    ex);

        }

    }

    public TokenResponse refreshToken(
            String refreshToken) {

        MultiValueMap<String, String> form =
                new LinkedMultiValueMap<>();

        form.add("grant_type", "refresh_token");
        form.add("client_id", keycloakProperties.getClientId());
        form.add("client_secret", keycloakProperties.getClientSecret());
        form.add("refresh_token", refreshToken);

        try {

            return restClient.post()

                    .uri(keycloakProperties.getServerUrl()
                            + "/realms/"
                            + keycloakProperties.getRealm()
                            + "/protocol/openid-connect/token")

                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)

                    .body(form)

                    .retrieve()

                    .body(TokenResponse.class);

        } catch (HttpStatusCodeException ex) {

            log.error("Keycloak returned: {}", ex.getResponseBodyAsString());

            throw new KeycloakException(
                    ex.getResponseBodyAsString(),
                    ex);

        }

    }

    public void logout(String refreshToken) {

        MultiValueMap<String, String> form =
                new LinkedMultiValueMap<>();

        form.add("client_id", keycloakProperties.getClientId());
        form.add("client_secret", keycloakProperties.getClientSecret());
        form.add("refresh_token", refreshToken);

        try {

            restClient.post()

                    .uri(keycloakProperties.getServerUrl()
                            + "/realms/"
                            + keycloakProperties.getRealm()
                            + "/protocol/openid-connect/logout")

                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)

                    .body(form)

                    .retrieve()

                    .toBodilessEntity();

        } catch (HttpStatusCodeException ex) {

            throw new KeycloakException(
                    ex.getResponseBodyAsString(),
                    ex);

        } catch (RestClientException ex) {

            throw new KeycloakException(
                    "Unable to logout from Keycloak.",
                    ex);

        }

    }


}