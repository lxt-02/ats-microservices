package com.ats.userservice.application.command.user;

import com.ats.userservice.domain.model.user.enums.UserRole;

public record ChangeUserRoleCommand(
        Long id,
        UserRole role,
        String updatedBy
) {
}
