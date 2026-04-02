package com.financeapp.dto.request;

import com.financeapp.model.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for updating a user's role (ADMIN only).
 */
@Data
public class UpdateRoleRequest {

    @NotNull(message = "Role is required")
    private Role role;
}
