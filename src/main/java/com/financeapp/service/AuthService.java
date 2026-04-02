package com.financeapp.service;

import com.financeapp.dto.request.LoginRequest;
import com.financeapp.dto.request.RegisterRequest;
import com.financeapp.dto.response.AuthResponse;

/**
 * Contract for authentication operations.
 */
public interface AuthService {

    /**
     * Register a new user with VIEWER role by default.
     *
     * @param request registration data
     * @return JWT AuthResponse for immediate login after registration
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticate a user and return a signed JWT.
     *
     * @param request login credentials
     * @return JWT AuthResponse
     */
    AuthResponse login(LoginRequest request);
}
