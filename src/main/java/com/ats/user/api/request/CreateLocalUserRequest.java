package com.ats.user.api.request;

import com.ats.user.application.command.CreateLocalUserCommand;
import com.ats.user.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLocalUserRequest(
        Long departmentId,
        @NotBlank(message = "Full name is required")
        @Size(max = 255, message = "Full name must not exceed 255 characters")
        String fullName,
        @NotBlank(message = "Email is required")
        @Email(message = "Email is invalid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,
        @Size(max = 255, message = "Password hash must not exceed 255 characters")
        String passwordHash,
        @Size(max = 30, message = "Phone must not exceed 30 characters")
        String phone,
        UserRole role
) {

    public CreateLocalUserCommand toCommand() {
        return new CreateLocalUserCommand(departmentId, fullName, email, passwordHash, phone, role);
    }
}
