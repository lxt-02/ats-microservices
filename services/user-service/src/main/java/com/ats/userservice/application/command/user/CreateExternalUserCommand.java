package com.ats.userservice.application.command.user;

import com.ats.userservice.domain.model.user.enums.AuthProvider;
import com.ats.userservice.domain.model.user.enums.UserRole;

public record CreateExternalUserCommand(
        Long departmentId,
        String fullName,
        String email,
        String phone,
        UserRole role,
        AuthProvider authProvider,
        String externalSubjectId,
        String createdBy
) {
}
