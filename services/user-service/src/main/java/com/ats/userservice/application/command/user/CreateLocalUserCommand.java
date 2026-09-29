package com.ats.userservice.application.command.user;

import com.ats.userservice.domain.model.user.enums.UserRole;

public record CreateLocalUserCommand(
        Long departmentId,
        String fullName,
        String email,
        String passwordHash,
        String phone,
        UserRole role,
        String createdBy
) {
}
