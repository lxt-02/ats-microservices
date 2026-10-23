package com.ats.user.api.request;

import com.ats.user.application.command.ChangeUserRoleCommand;
import com.ats.user.domain.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public record ChangeUserRoleRequest(
        @NotNull(message = "User role is required")
        UserRole role
) {

    public ChangeUserRoleCommand toCommand(Long id) {
        return new ChangeUserRoleCommand(id, role);
    }
}
