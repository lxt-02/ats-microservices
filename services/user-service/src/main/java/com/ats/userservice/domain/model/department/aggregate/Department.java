package com.ats.userservice.domain.model.department.aggregate;

import com.ats.userservice.domain.model.department.exception.DepartmentDomainException;
import com.ats.userservice.domain.model.department.valueobject.DepartmentId;

import java.time.Instant;

public class Department {

    private final DepartmentId id;
    private DepartmentId parentId;
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private boolean deleted;
    private Instant deletedAt;

    private Department(DepartmentId id, DepartmentId parentId, String name, String description,
                       Instant createdAt, Instant updatedAt, String createdBy, String updatedBy,
                       boolean deleted, Instant deletedAt) {
        this.id = id;
        this.parentId = parentId;
        this.name = requireText(name, "Department name is required");
        this.description = normalizeNullable(description);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = normalizeNullable(createdBy);
        this.updatedBy = normalizeNullable(updatedBy);
        this.deleted = deleted;
        this.deletedAt = deletedAt;
        ensureParentIsNotSelf(parentId);
    }

    public static Department create(DepartmentId parentId, String name, String description, String createdBy) {
        Instant now = Instant.now();
        return new Department(null, parentId, name, description, now, now, createdBy, createdBy, false, null);
    }

    public static Department restore(DepartmentId id, DepartmentId parentId, String name, String description,
                                     Instant createdAt, Instant updatedAt, String createdBy, String updatedBy,
                                     boolean deleted, Instant deletedAt) {
        return new Department(id, parentId, name, description, createdAt, updatedAt, createdBy, updatedBy,
                deleted, deletedAt);
    }

    public void rename(String name, String updatedBy) {
        ensureNotDeleted();
        this.name = requireText(name, "Department name is required");
        markUpdated(updatedBy);
    }

    public void changeParent(DepartmentId parentId, String updatedBy) {
        ensureNotDeleted();
        ensureParentIsNotSelf(parentId);
        this.parentId = parentId;
        markUpdated(updatedBy);
    }

    public void changeDescription(String description, String updatedBy) {
        ensureNotDeleted();
        this.description = normalizeNullable(description);
        markUpdated(updatedBy);
    }

    public void markDeleted(String updatedBy) {
        if (deleted) {
            return;
        }
        this.deleted = true;
        this.deletedAt = Instant.now();
        markUpdated(updatedBy);
    }

    private void ensureParentIsNotSelf(DepartmentId parentId) {
        if (id != null && parentId != null && id.equals(parentId)) {
            throw new DepartmentDomainException("Department cannot be its own parent");
        }
    }

    private void ensureNotDeleted() {
        if (deleted) {
            throw new DepartmentDomainException("Deleted department cannot be changed");
        }
    }

    private void markUpdated(String updatedBy) {
        this.updatedAt = Instant.now();
        this.updatedBy = normalizeNullable(updatedBy);
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private static String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public DepartmentId getId() {
        return id;
    }

    public DepartmentId getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
