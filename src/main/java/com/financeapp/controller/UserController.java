package com.financeapp.controller;

import com.financeapp.dto.request.UpdateRoleRequest;
import com.financeapp.dto.response.UserResponse;
import com.financeapp.model.enums.UserStatus;
import com.financeapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user management.
 *
 * Base path: /api/users
 * ALL endpoints require ADMIN role.
 *
 * Route-level security is enforced in SecurityConfig.
 * Method-level @PreAuthorize provides a second layer of defence.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")           // class-level guard
@Tag(name = "User Management", description = "Admin-only user management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users
     * Retrieve all users in the system.
     */
    @GetMapping
    @Operation(summary = "List all users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * GET /api/users/{id}
     * Retrieve a specific user.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * PATCH /api/users/{id}/role
     * Assign a new role to a user.
     */
    @PatchMapping("/{id}/role")
    @Operation(summary = "Update user role",
               description = "Assigns VIEWER, ANALYST, or ADMIN role to the target user.")
    public ResponseEntity<UserResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        return ResponseEntity.ok(userService.updateRole(id, request));
    }

    /**
     * PATCH /api/users/{id}/activate
     * Activate a user account.
     */
    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a user account")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.updateStatus(id, UserStatus.ACTIVE));
    }

    /**
     * PATCH /api/users/{id}/deactivate
     * Deactivate (disable) a user account.
     * The user's JWT will be rejected on next request because isAccountNonLocked()
     * checks the status in real-time from the DB.
     */
    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a user account",
               description = "Marks the user as INACTIVE. Existing tokens become invalid immediately.")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.updateStatus(id, UserStatus.INACTIVE));
    }
}
