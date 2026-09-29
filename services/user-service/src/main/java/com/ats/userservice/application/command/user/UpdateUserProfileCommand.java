package com.ats.userservice.application.command.user;

public record UpdateUserProfileCommand(
        Long id,
        Long departmentId,
        String fullName,
        String phone,
        String updatedBy
) {
}
