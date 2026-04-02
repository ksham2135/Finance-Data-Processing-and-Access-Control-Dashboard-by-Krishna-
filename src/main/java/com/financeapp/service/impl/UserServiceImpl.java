package com.financeapp.service.impl;

import com.financeapp.dto.request.UpdateRoleRequest;
import com.financeapp.dto.response.UserResponse;
import com.financeapp.exception.ResourceNotFoundException;
import com.financeapp.model.entity.User;
import com.financeapp.model.enums.UserStatus;
import com.financeapp.repository.UserRepository;
import com.financeapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link UserService}.
 *
 * All write operations are wrapped in a transaction.
 * User entities are mapped to DTOs before being returned
 * so the password hash is never leaked to the client.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // -------------------------------------------------------
    // Read operations
    // -------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return mapToResponse(findUserOrThrow(id));
    }

    // -------------------------------------------------------
    // Write operations
    // -------------------------------------------------------

    @Override
    @Transactional
    public UserResponse updateRole(Long userId, UpdateRoleRequest request) {
        User user = findUserOrThrow(userId);
        user.setRole(request.getRole());
        userRepository.save(user);
        log.info("Role updated: userId={}, newRole={}", userId, request.getRole());
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateStatus(Long userId, UserStatus status) {
        User user = findUserOrThrow(userId);
        user.setStatus(status);
        userRepository.save(user);
        log.info("Status updated: userId={}, newStatus={}", userId, status);
        return mapToResponse(user);
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    /**
     * Maps a User entity to a safe response DTO.
     * Password hash is intentionally excluded.
     */
    UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
