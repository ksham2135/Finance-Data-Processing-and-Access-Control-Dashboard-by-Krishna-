package com.financeapp.service.impl;

import com.financeapp.dto.request.LoginRequest;
import com.financeapp.dto.request.RegisterRequest;
import com.financeapp.dto.response.AuthResponse;
import com.financeapp.exception.BusinessException;
import com.financeapp.model.entity.User;
import com.financeapp.model.enums.Role;
import com.financeapp.model.enums.UserStatus;
import com.financeapp.repository.UserRepository;
import com.financeapp.security.jwt.JwtTokenProvider;
import com.financeapp.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link AuthService}.
 *
 * Handles user registration and JWT-based login.
 * Passwords are always stored as BCrypt hashes — never plaintext.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtTokenProvider      jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    // -------------------------------------------------------
    // Register
    // -------------------------------------------------------

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Guard: reject duplicate emails
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                "An account with email '" + request.getEmail() + "' already exists.");
        }

        // Build and persist the new user (default role: VIEWER)
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.VIEWER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);
        log.info("New user registered: email={}, role={}", user.getEmail(), user.getRole());

        // Issue a JWT immediately so clients can use the app right away
        String token = jwtTokenProvider.generateToken(user, user.getRole().name());
        return buildAuthResponse(user, token);
    }

    // -------------------------------------------------------
    // Login
    // -------------------------------------------------------

    @Override
    public AuthResponse login(LoginRequest request) {
        // Spring Security handles credential verification and throws on bad input
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        User user = (User) auth.getPrincipal();
        String token = jwtTokenProvider.generateToken(user, user.getRole().name());

        log.info("User logged in: email={}, role={}", user.getEmail(), user.getRole());
        return buildAuthResponse(user, token);
    }

    // -------------------------------------------------------
    // Helper
    // -------------------------------------------------------

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
