package com.ats.user.application.command;

public record UpdateUserProfileCommand(Long id, Long departmentId, String fullName, String phone) {
}
