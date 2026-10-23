package com.ats.user.infrastructure.persistence.converter;

import com.ats.user.domain.aggregate.User;
import com.ats.user.domain.enums.AuthProvider;
import com.ats.user.domain.enums.UserRole;
import com.ats.user.domain.enums.UserStatus;
import com.ats.user.domain.valueobject.Email;
import com.ats.user.domain.valueobject.PhoneNumber;
import com.ats.user.domain.valueobject.UserId;
import com.ats.user.infrastructure.persistence.record.UserRecord;

/** Record &lt;-&gt; aggregate. Nullable legacy columns fall back to safe defaults. */
public final class UserPersistenceConverter {

    private UserPersistenceConverter() {
    }

    public static UserRecord toRecord(User user) {
        UserRecord record = new UserRecord();
        record.setId(user.getId() == null ? null : user.getId().value());
        record.setDepartmentId(user.getDepartmentId());
        record.setFullName(user.getFullName());
        record.setEmail(user.getEmail().value());
        record.setPasswordHash(user.getPasswordHash());
        record.setPhone(user.getPhoneNumber() == null ? null : user.getPhoneNumber().value());
        record.setRole(user.getRole().name());
        record.setAuthProvider(user.getAuthProvider().name());
        record.setExternalSubjectId(user.getExternalSubjectId());
        record.setStatus(user.getStatus().name());
        record.setCreatedAt(user.getCreatedAt());
        record.setUpdatedAt(user.getUpdatedAt());
        record.setCreatedBy(user.getCreatedBy());
        record.setUpdatedBy(user.getUpdatedBy());
        record.setDeleted(user.isDeleted());
        record.setDeletedAt(user.getDeletedAt());
        return record;
    }

    public static User toAggregate(UserRecord record) {
        return User.restore(
                UserId.of(record.getId()),
                record.getDepartmentId(),
                record.getFullName(),
                Email.of(record.getEmail()),
                record.getPasswordHash(),
                PhoneNumber.of(record.getPhone()),
                record.getRole() == null ? UserRole.CANDIDATE : UserRole.valueOf(record.getRole()),
                record.getAuthProvider() == null ? AuthProvider.LOCAL : AuthProvider.valueOf(record.getAuthProvider()),
                record.getExternalSubjectId(),
                record.getStatus() == null ? UserStatus.INACTIVE : UserStatus.valueOf(record.getStatus()),
                record.getCreatedAt(),
                record.getUpdatedAt(),
                record.getCreatedBy(),
                record.getUpdatedBy(),
                record.isDeleted(),
                record.getDeletedAt()
        );
    }
}
