package com.filestorageservice.authservice.controller;

import com.filestorageservice.authservice.exception.EmailNotVerifiedException;
import com.filestorageservice.authservice.payload.*;
import com.filestorageservice.authservice.service.service_interface.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping("/api/v1/auth")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class AuthController {

    @Autowired
    private AuthService authService;


    @PostMapping("/login")
    public Mono<ResponseEntity<?>> login(@RequestBody AuthLoginPayload authLoginPayload) {
        try {
            return authService.login(authLoginPayload).map(response -> {
                log.info("auth respoonse : {}", response);
                ResponseCookie accessToken = ResponseCookie.from("ACCESS-TOKEN", response.getAccessToken())
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(60 * 60)
                        .sameSite("Strict")
                        .build();

                ResponseCookie refreshToken = ResponseCookie.from("REFRESH-TOKEN", response.getRefreshToken())
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(24 * 60 * 60)
                        .sameSite("Strict")
                        .build();
                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, accessToken.toString())
                        .header(HttpHeaders.SET_COOKIE, refreshToken.toString())
                        .body(ApiResponse.success("Log in successful", response.getUser()));
            });
        } catch (Exception e) {
            e.printStackTrace();
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(e.getMessage())));
        }
    }

    @PostMapping("/login/{provider}")
    public ResponseEntity<?> login(@PathVariable String provider) {
        try {
            String authorizationUrl = authService.getAuthorizationUrl(provider);
            log.info("authorization url : {}", authorizationUrl);
            return ResponseEntity.status(HttpStatus.OK).body(authorizationUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<Object>> register(@RequestBody AuthRegisterPayload authRegisterPayload) {
        log.info("register request : {}", authRegisterPayload);
        return authService.registerUser(authRegisterPayload)
                .map(registeredUser ->
                        // Explicitly set the generic type to Object
                        ResponseEntity.<Object>ok(ApiResponse.success("User registered successfully", registeredUser))
                )
                .onErrorResume(error -> {
                    log.error("Error during registration: ", error);
                    return Mono.just(
                            // Explicitly set the generic type to Object
                            ResponseEntity.<Object>status(HttpStatus.BAD_REQUEST)
                                    .body(ApiResponse.error(error.getMessage()))
                    );
                });
    }

    @DeleteMapping("/delete-account")
    public Mono<ResponseEntity<?>> deleteAccount(@RequestParam("id") String id) {
        return authService.deleteAccount(id)
                .then(Mono.just(ResponseEntity.status(200).body(ApiResponse.success("User deleted successfully", true))));
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<?> handleEmailNotVerifiedException(EmailNotVerifiedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
    }
}
