package com.ats.userservice.domain.model.department.valueobject;

public record DepartmentId(Long value) {

    public DepartmentId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("Department id must be positive");
        }
    }

    public static DepartmentId of(Long value) {
        return new DepartmentId(value);
    }
}
