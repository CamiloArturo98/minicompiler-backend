package com.minicompiler.service;

import com.minicompiler.domain.entity.User;
import com.minicompiler.domain.repository.UserRepository;
import com.minicompiler.dto.request.LoginRequest;
import com.minicompiler.dto.request.RegisterRequest;
import com.minicompiler.dto.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles user registration and authentication, returning JWT tokens on success.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    // =========================================================================
    // Constants
    // =========================================================================

    private static final String ERR_USERNAME_TAKEN = "Username already taken: ";
    private static final String ERR_EMAIL_TAKEN    = "Email already registered: ";

    // =========================================================================
    // Dependencies
    // =========================================================================

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtService            jwtService;
    private final AuthenticationManager authenticationManager;

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Registers a new user, hashes their password, persists them, and returns a JWT.
     *
     * @param  request the validated registration request
     * @return an {@link AuthResponse} containing the JWT and user info
     * @throws IllegalArgumentException if the username or email is already taken
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException(ERR_USERNAME_TAKEN + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(ERR_EMAIL_TAKEN + request.email());
        }

        var user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        userRepository.save(user);
        return buildResponse(user);
    }

    /**
     * Authenticates a user by username and password, returning a JWT on success.
     *
     * @param  request the validated login request
     * @return an {@link AuthResponse} containing the JWT and user info
     */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        var user = userRepository.findByUsername(request.username()).orElseThrow();
        return buildResponse(user);
    }

    // =========================================================================
    // Factory Method
    // =========================================================================

    private AuthResponse buildResponse(User user) {
        return new AuthResponse(
                jwtService.generateToken(user),
                user.getUsername(),
                user.getRole().name()
        );
    }
}