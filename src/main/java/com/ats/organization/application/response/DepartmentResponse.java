package com.ats.organization.application.response;

import com.ats.organization.domain.aggregate.Department;

import java.time.Instant;

public record DepartmentResponse(
        Long id,
        Long parentId,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {

    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(
                department.getId() == null ? null : department.getId().value(),
                department.getParentId() == null ? null : department.getParentId().value(),
                department.getName(),
                department.getDescription(),
                department.getCreatedAt(),
                department.getUpdatedAt(),
                department.getCreatedBy(),
                department.getUpdatedBy()
        );
    }
}
