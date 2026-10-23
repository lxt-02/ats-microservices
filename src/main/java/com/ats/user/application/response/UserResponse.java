package com.ats.user.application.response;

import com.ats.user.domain.aggregate.User;

import java.time.Instant;

/** Enum values are exposed as their names so other modules do not depend on this module's domain. */
public record UserResponse(
        Long id,
        Long departmentId,
        String fullName,
        String email,
        String phone,
        String role,
        String authProvider,
        String externalSubjectId,
        String status,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId() == null ? null : user.getId().value(),
                user.getDepartmentId(),
                user.getFullName(),
                user.getEmail().value(),
                user.getPhoneNumber() == null ? null : user.getPhoneNumber().value(),
                user.getRole().name(),
                user.getAuthProvider().name(),
                user.getExternalSubjectId(),
                user.getStatus().name(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getCreatedBy(),
                user.getUpdatedBy()
        );
    }
}
