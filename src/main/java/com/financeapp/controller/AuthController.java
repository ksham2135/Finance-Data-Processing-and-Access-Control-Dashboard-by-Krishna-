package com.financeapp.controller;

import com.financeapp.dto.request.LoginRequest;
import com.financeapp.dto.request.RegisterRequest;
import com.financeapp.dto.response.AuthResponse;
import com.financeapp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication (register, login).
 *
 * Base path: /api/auth
 * All endpoints are PUBLIC — no JWT required.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login endpoints")
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register
     * Register a new user account (default role: VIEWER).
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user",
               description = "Creates a new account with VIEWER role. Returns JWT on success.")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/login
     * Authenticate with credentials and receive a JWT.
     */
    @PostMapping("/login")
    @Operation(summary = "Login",
               description = "Authenticates the user and returns a Bearer JWT token.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
