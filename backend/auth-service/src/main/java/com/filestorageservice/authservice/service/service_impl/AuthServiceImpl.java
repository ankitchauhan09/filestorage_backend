package com.filestorageservice.authservice.service.service_impl;

import com.filestorageservice.authservice.exception.EmailNotVerifiedException;
import com.filestorageservice.authservice.model.User;
import com.filestorageservice.authservice.payload.AuthLoginPayload;
import com.filestorageservice.authservice.payload.AuthRegisterPayload;
import com.filestorageservice.authservice.payload.AuthResponse;
import com.filestorageservice.authservice.service.service_interface.AuthService;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Value("${authorization.redirect.uri}")
    private String authRedirectUri;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.clientId}")
    private String clientId;
    @Value("${keycloak.clientSecret}")
    private String clientSecret;
    @Value("${keycloak.server.url}")
    private String serverUrl;
    @Autowired
    private WebClient webClient;
    @Autowired
    private RestClientAutoConfiguration restClientAutoConfiguration;
    @Autowired
    private Keycloak keycloak;


    @Override
    public Mono<AuthResponse> login(AuthLoginPayload authLoginPayload) {
        try {
            Boolean isEmailVerified = checkIsEmailVerified(authLoginPayload.getEmail());
            if (!isEmailVerified) {
                throw new EmailNotVerifiedException("Email not verified");
            }
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "password");
            formData.add("client_id", clientId);
            formData.add("client_secret", clientSecret);
            formData.add("username", authLoginPayload.getEmail());
            formData.add("password", authLoginPayload.getPassword());

            String tokenEndpoint = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

            return webClient.post()
                    .uri(tokenEndpoint)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                        return clientResponse.bodyToMono(Map.class)
                                .flatMap(errorBody -> {
                                    String error = (String) errorBody.get("error");
                                    String errorDescription = (String) errorBody.get("error_description");
                                    log.error("Keycloak error: {} - {}", error, errorDescription);
                                    return Mono.error(new RuntimeException("Keycloak authentication failed: " + errorDescription));
                                });
                    })
                    .bodyToMono(Map.class)
                    .flatMap(response -> {
                        String accessToken = response.get("access_token").toString();
                        String refreshToken = response.get("refresh_token").toString();

                        return getUserFromEmail(authLoginPayload.getEmail())
                                .map(user -> AuthResponse.builder().user(user).accessToken(accessToken).refreshToken(refreshToken).build())
                                .switchIfEmpty(Mono.error(new RuntimeException("User not found in user service")))
                                .onErrorResume(error -> Mono.error(new RuntimeException("Error fetching user from user service: " + error.getMessage())));
                    });
        } catch (EmailNotVerifiedException e) {
            throw new EmailNotVerifiedException("Verify the email before logging in...");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    private boolean checkIsEmailVerified(String email) {
        try {
            List<UserRepresentation> users = keycloak.realm(realm).users().search(email, true);
            return users.get(0).isEmailVerified();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    Mono<User> getUserFromEmail(String email) {
        return webClient
                .get()
                .uri("https://localhost:8002/api/v1/user?email=" + email)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.statusCode().equals(HttpStatus.NOT_FOUND)
                                ? Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)) // Create specific exception
                                : response.createException().flatMap(Mono::error)
                )
                .bodyToMono(User.class)
                .onErrorResume(ResponseStatusException.class, ex -> {
                    if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.empty(); // Handle 404 by returning empty
                    }
                    return Mono.error(ex); // Propagate other errors
                });
    }

    @Override
    public String getAuthorizationUrl(String provider) {
        String state = UUID.randomUUID().toString();
        String encodedURIString = UriUtils.encode(authRedirectUri, StandardCharsets.UTF_8);

        return UriComponentsBuilder.fromUriString(serverUrl)
                .path("/realms/" + realm + "/protocol/openid-connect/auth")
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", encodedURIString)
                .queryParam("scope", "openid")
                .queryParam("state", state)
                .queryParam("kc_idp_hint", provider.toLowerCase().trim())
                .buildAndExpand(realm)
                .toUriString();
    }

    @Override
    public Mono<User> registerUser(AuthRegisterPayload authRegisterPayload) {
        return getUserFromEmail(authRegisterPayload.getEmail())
                .flatMap(existingUser -> Mono.<User>error(new RuntimeException("User already exists")))
                .switchIfEmpty(Mono.defer(() -> {
                    User user = User.builder().email(authRegisterPayload.getEmail()).firstName(authRegisterPayload.getFirstName()).lastName(authRegisterPayload.getLastName()).build();
                    String userId = createKeycloakuser(user, authRegisterPayload.getPassword());
                    user.setUserId(userId);
//                    return Mono.just(user);   

                    return webClient.post()
                            .uri("https://localhost:8002/api/v1/user/create")
                            .bodyValue(user)
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, response ->
                                    response.bodyToMono(String.class)
                                            .flatMap(errorMessage -> Mono.error(new RuntimeException("Failed to save the user : " + errorMessage))))
                            .onStatus(HttpStatusCode::is5xxServerError, response ->
                                    response.bodyToMono(String.class)
                                            .flatMap(errorMessage -> Mono.error(new RuntimeException("Failed to save the user : " + errorMessage))))
                            .bodyToMono(User.class);
                }));
    }

    @Override
    public Mono<Void> deleteAccount(String id) {
        try {
            keycloak.realm(realm).users().get(id).remove();
            return webClient.delete()
                    .uri("https://localhost:8002/api/v1/user/" + id)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)))
                    .bodyToMono(Void.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String createKeycloakuser(User user, String password) {
        UserRepresentation keycloakUser = new UserRepresentation();
        keycloakUser.setEmail(user.getEmail());
        keycloakUser.setFirstName(user.getFirstName());
        keycloakUser.setLastName(user.getLastName());
        keycloakUser.setUsername(user.getEmail());
        keycloakUser.setEnabled(true);
        keycloakUser.setEmailVerified(false);

        log.info("Creating Keycloak user: {}", keycloakUser);

        Response response = keycloak.realm(realm).users().create(keycloakUser);
        log.info("Keycloak response status: {}", response.getStatus());
        log.info("Keycloak response location: {}", response.getLocation());

        if (response.getStatus() != 201) {
            String errorMessage = "Failed to create Keycloak user. Status: " + response.getStatus();
            log.error(errorMessage);
            log.error("Response body: {}", response.readEntity(String.class)); // Log the response body
            throw new RuntimeException(errorMessage);
        }

        String userId = CreatedResponseUtil.getCreatedId(response);
        log.info("Keycloak user created with ID: {}", userId);

        setKeycloakPassword(userId, password);
        emailVerification(userId);
        return userId;
    }

    private void emailVerification(String userId) {
        keycloak.realm(realm).users().get(userId).sendVerifyEmail();
    }

    private void setKeycloakPassword(String userId, String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        keycloak.realm(realm).users().get(userId).resetPassword(credential);
    }


}
