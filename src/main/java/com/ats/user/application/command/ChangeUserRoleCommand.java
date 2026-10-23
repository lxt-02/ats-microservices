package com.ats.user.application.command;

import com.ats.user.domain.enums.UserRole;

public record ChangeUserRoleCommand(Long id, UserRole role) {
}
