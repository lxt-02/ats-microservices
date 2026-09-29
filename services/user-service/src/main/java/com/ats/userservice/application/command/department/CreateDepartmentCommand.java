package com.ats.userservice.application.command.department;

public record CreateDepartmentCommand(
        Long parentId,
        String name,
        String description,
        String createdBy
) {
}
