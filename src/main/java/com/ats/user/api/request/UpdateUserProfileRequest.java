package com.ats.user.api.request;

import com.ats.user.application.command.UpdateUserProfileCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        Long departmentId,
        @NotBlank(message = "Full name is required")
        @Size(max = 255, message = "Full name must not exceed 255 characters")
        String fullName,
        @Size(max = 30, message = "Phone must not exceed 30 characters")
        String phone
) {

    public UpdateUserProfileCommand toCommand(Long id) {
        return new UpdateUserProfileCommand(id, departmentId, fullName, phone);
    }
}
