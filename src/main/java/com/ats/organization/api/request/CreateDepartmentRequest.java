package com.ats.organization.api.request;

import com.ats.organization.application.command.CreateDepartmentCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDepartmentRequest(
        Long parentId,
        @NotBlank(message = "Department name is required")
        @Size(max = 255, message = "Department name must not exceed 255 characters")
        String name,
        String description
) {

    public CreateDepartmentCommand toCommand() {
        return new CreateDepartmentCommand(parentId, name, description);
    }
}
