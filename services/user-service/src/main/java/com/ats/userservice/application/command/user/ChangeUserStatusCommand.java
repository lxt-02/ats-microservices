package com.ats.userservice.application.command.user;

import com.ats.userservice.domain.model.user.enums.UserStatus;

public record ChangeUserStatusCommand(
        Long id,
        UserStatus status,
        String updatedBy
) {
}
