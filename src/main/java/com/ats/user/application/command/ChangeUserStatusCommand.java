package com.ats.user.application.command;

import com.ats.user.domain.enums.UserStatus;

public record ChangeUserStatusCommand(Long id, UserStatus status) {
}
