package com.ats.user.api.request;

import com.ats.user.application.command.ChangeUserStatusCommand;
import com.ats.user.domain.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeUserStatusRequest(
        @NotNull(message = "User status is required")
        UserStatus status
) {

    public ChangeUserStatusCommand toCommand(Long id) {
        return new ChangeUserStatusCommand(id, status);
    }
}
