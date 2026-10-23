package com.ats.organization.domain.valueobject;

public record DepartmentId(Long value) {

    public DepartmentId {
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("Department id must be positive");
        }
    }

    public static DepartmentId of(Long value) {
        return new DepartmentId(value);
    }

    /** Null-safe factory for optional references. */
    public static DepartmentId ofNullable(Long value) {
        return value == null ? null : new DepartmentId(value);
    }
}
