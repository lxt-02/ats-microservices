package com.ats.organization.application.command;

public record UpdateDepartmentCommand(Long id, Long parentId, String name, String description) {
}
