package com.filestorageservice.authservice.service.service_interface;

import com.filestorageservice.authservice.model.User;
import com.filestorageservice.authservice.payload.AuthLoginPayload;
import com.filestorageservice.authservice.payload.AuthRegisterPayload;
import com.filestorageservice.authservice.payload.AuthResponse;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface AuthService {

    Mono<AuthResponse> login(AuthLoginPayload authLoginPayload);

    String getAuthorizationUrl(String provider);

    Mono<User> registerUser(AuthRegisterPayload authRegisterPayload);

    Mono<Void> deleteAccount(String id);
}
