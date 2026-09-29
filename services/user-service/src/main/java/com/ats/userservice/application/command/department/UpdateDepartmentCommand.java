package com.ats.userservice.application.command.department;

public record UpdateDepartmentCommand(
        Long id,
        Long parentId,
        String name,
        String description,
        String updatedBy
) {
}
