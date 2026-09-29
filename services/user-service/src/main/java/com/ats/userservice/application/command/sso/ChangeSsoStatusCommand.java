package com.ats.userservice.application.command.sso;

public record ChangeSsoStatusCommand(
        Long id,
        boolean active,
        String updatedBy
) {
}
