package com.ats.user.application.command;

import com.ats.user.domain.enums.UserRole;

public record CreateLocalUserCommand(
        Long departmentId,
        String fullName,
        String email,
        String passwordHash,
        String phone,
        UserRole role
) {
}
