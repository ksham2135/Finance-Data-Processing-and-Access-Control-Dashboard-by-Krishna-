package com.financeapp.service;

import com.financeapp.dto.request.UpdateRoleRequest;
import com.financeapp.dto.response.UserResponse;
import com.financeapp.model.enums.UserStatus;

import java.util.List;

/**
 * Contract for user management operations.
 */
public interface UserService {

    /** Get all users in the system */
    List<UserResponse> getAllUsers();

    /** Get a single user by ID */
    UserResponse getUserById(Long id);

    /**
     * Assign a new role to a user.
     *
     * @param userId  target user
     * @param request new role
     * @return updated UserResponse
     */
    UserResponse updateRole(Long userId, UpdateRoleRequest request);

    /**
     * Set a user's status to ACTIVE or INACTIVE.
     *
     * @param userId target user
     * @param status new status
     * @return updated UserResponse
     */
    UserResponse updateStatus(Long userId, UserStatus status);
}
