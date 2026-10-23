package com.ats.organization.api.request;

import com.ats.organization.application.command.UpdateDepartmentCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDepartmentRequest(
        Long parentId,
        @NotBlank(message = "Department name is required")
        @Size(max = 255, message = "Department name must not exceed 255 characters")
        String name,
        String description
) {

    public UpdateDepartmentCommand toCommand(Long id) {
        return new UpdateDepartmentCommand(id, parentId, name, description);
    }
}
