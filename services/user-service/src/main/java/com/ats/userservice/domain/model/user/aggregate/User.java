package com.ats.userservice.domain.model.user.aggregate;

import com.ats.userservice.domain.model.department.valueobject.DepartmentId;
import com.ats.userservice.domain.model.user.enums.AuthProvider;
import com.ats.userservice.domain.model.user.enums.UserRole;
import com.ats.userservice.domain.model.user.enums.UserStatus;
import com.ats.userservice.domain.model.user.exception.UserDomainException;
import com.ats.userservice.domain.model.user.valueobject.Email;
import com.ats.userservice.domain.model.user.valueobject.PhoneNumber;
import com.ats.userservice.domain.model.user.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

public class User {

    private final UserId id;
    private DepartmentId departmentId;
    private String fullName;
    private Email email;
    private String passwordHash;
    private PhoneNumber phoneNumber;
    private UserRole role;
    private AuthProvider authProvider;
    private String externalSubjectId;
    private UserStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private boolean deleted;
    private Instant deletedAt;

    private User(UserId id, DepartmentId departmentId, String fullName, Email email, String passwordHash,
                 PhoneNumber phoneNumber, UserRole role, AuthProvider authProvider, String externalSubjectId,
                 UserStatus status, Instant createdAt, Instant updatedAt, String createdBy,
                 String updatedBy, boolean deleted, Instant deletedAt) {
        this.id = id;
        this.departmentId = departmentId;
        this.fullName = requireText(fullName, "Full name is required");
        this.email = Objects.requireNonNull(email, "Email is required");
        this.phoneNumber = phoneNumber;
        this.role = Objects.requireNonNull(role, "User role is required");
        this.authProvider = Objects.requireNonNull(authProvider, "Auth provider is required");
        this.externalSubjectId = normalizeNullable(externalSubjectId);
        this.passwordHash = normalizeNullable(passwordHash);
        this.status = Objects.requireNonNull(status, "User status is required");
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = normalizeNullable(createdBy);
        this.updatedBy = normalizeNullable(updatedBy);
        this.deleted = deleted;
        this.deletedAt = deletedAt;
        validateAuthenticationIdentity();
    }

    public static User create(String fullName, Email email, String passwordHash, PhoneNumber phoneNumber,
                              UserRole role, DepartmentId departmentId, String createdBy) {
        Instant now = Instant.now();
        return new User(null, departmentId, fullName, email, passwordHash, phoneNumber, role, AuthProvider.LOCAL, null,
                UserStatus.ACTIVE, now, now, createdBy, createdBy, false, null);
    }

    public static User createExternal(String fullName, Email email, PhoneNumber phoneNumber, UserRole role,
                                      DepartmentId departmentId, AuthProvider authProvider,
                                      String externalSubjectId, String createdBy) {
        Instant now = Instant.now();
        return new User(null, departmentId, fullName, email, null, phoneNumber, role, authProvider,
                externalSubjectId, UserStatus.ACTIVE, now, now, createdBy, createdBy, false, null);
    }

    public static User restore(UserId id, DepartmentId departmentId, String fullName, Email email,
                               String passwordHash, PhoneNumber phoneNumber, UserRole role,
                               AuthProvider authProvider, String externalSubjectId, UserStatus status, Instant createdAt,
                               Instant updatedAt, String createdBy, String updatedBy,
                               boolean deleted, Instant deletedAt) {
        return new User(id, departmentId, fullName, email, passwordHash, phoneNumber, role, authProvider, externalSubjectId,
                status, createdAt, updatedAt, createdBy, updatedBy, deleted, deletedAt);
    }

    public void changeProfile(String fullName, PhoneNumber phoneNumber, DepartmentId departmentId, String updatedBy) {
        ensureNotDeleted();
        this.fullName = requireText(fullName, "Full name is required");
        this.phoneNumber = phoneNumber;
        this.departmentId = departmentId;
        markUpdated(updatedBy);
    }

    public void changeRole(UserRole role, String updatedBy) {
        ensureNotDeleted();
        this.role = Objects.requireNonNull(role, "User role is required");
        markUpdated(updatedBy);
    }

    public void linkExternalIdentity(AuthProvider authProvider, String externalSubjectId, String updatedBy) {
        ensureNotDeleted();
        this.authProvider = Objects.requireNonNull(authProvider, "Auth provider is required");
        this.externalSubjectId = normalizeNullable(externalSubjectId);
        validateAuthenticationIdentity();
        markUpdated(updatedBy);
    }

    public void activate(String updatedBy) {
        ensureNotDeleted();
        this.status = UserStatus.ACTIVE;
        markUpdated(updatedBy);
    }

    public void deactivate(String updatedBy) {
        ensureNotDeleted();
        this.status = UserStatus.INACTIVE;
        markUpdated(updatedBy);
    }

    public void lock(String updatedBy) {
        ensureNotDeleted();
        this.status = UserStatus.LOCKED;
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

    private void ensureNotDeleted() {
        if (deleted) {
            throw new UserDomainException("Deleted user cannot be changed");
        }
    }

    private void validateAuthenticationIdentity() {
        if (authProvider == AuthProvider.LOCAL) {
            return;
        }

        if (externalSubjectId == null) {
            throw new UserDomainException("External subject id is required for external auth provider");
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

    public UserId getId() {
        return id;
    }

    public DepartmentId getDepartmentId() {
        return departmentId;
    }

    public String getFullName() {
        return fullName;
    }

    public Email getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public UserRole getRole() {
        return role;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public String getExternalSubjectId() {
        return externalSubjectId;
    }

    public UserStatus getStatus() {
        return status;
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
