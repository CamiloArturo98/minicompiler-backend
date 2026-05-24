package com.minicompiler.controller;

import com.minicompiler.dto.request.LoginRequest;
import com.minicompiler.dto.request.RegisterRequest;
import com.minicompiler.dto.response.AuthResponse;
import com.minicompiler.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing authentication endpoints for registration and login.
 * All endpoints under {@code /api/v1/auth} are publicly accessible.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user and returns a JWT.
     *
     * @param  request the validated registration body
     * @return {@code 201 Created} with the JWT and user info
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request for username={}", request.username());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * Authenticates an existing user and returns a JWT.
     *
     * @param  request the validated login body
     * @return {@code 200 OK} with the JWT and user info
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for username={}", request.username());
        return ResponseEntity.ok(authService.login(request));
    }

}