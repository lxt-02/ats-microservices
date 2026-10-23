package com.ats.user.application.command;

import com.ats.user.domain.enums.AuthProvider;
import com.ats.user.domain.enums.UserRole;

public record CreateExternalUserCommand(
        Long departmentId,
        String fullName,
        String email,
        String phone,
        UserRole role,
        AuthProvider authProvider,
        String externalSubjectId
) {
}
