package com.ats.organization.application.command;

public record CreateDepartmentCommand(Long parentId, String name, String description) {
}
