package com.ats.userservice.infrastructure.mapper;

import com.ats.userservice.domain.model.department.valueobject.DepartmentId;
import com.ats.userservice.domain.model.user.aggregate.User;
import com.ats.userservice.domain.model.user.enums.AuthProvider;
import com.ats.userservice.domain.model.user.enums.UserRole;
import com.ats.userservice.domain.model.user.enums.UserStatus;
import com.ats.userservice.domain.model.user.valueobject.Email;
import com.ats.userservice.domain.model.user.valueobject.PhoneNumber;
import com.ats.userservice.domain.model.user.valueobject.UserId;
import com.ats.userservice.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {

    public UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId() == null ? null : user.getId().value());
        entity.setDepartmentId(user.getDepartmentId() == null ? null : user.getDepartmentId().value());
        entity.setFullName(user.getFullName());
        entity.setEmail(user.getEmail().value());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setPhone(user.getPhoneNumber() == null ? null : user.getPhoneNumber().value());
        entity.setRole(user.getRole());
        entity.setAuthProvider(user.getAuthProvider());
        entity.setExternalSubjectId(user.getExternalSubjectId());
        entity.setStatus(user.getStatus());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        entity.setCreatedBy(user.getCreatedBy());
        entity.setUpdatedBy(user.getUpdatedBy());
        entity.setDeleted(user.isDeleted());
        entity.setDeletedAt(user.getDeletedAt());
        return entity;
    }

    public User toDomain(UserJpaEntity entity) {
        return User.restore(
                UserId.of(entity.getId()),
                entity.getDepartmentId() == null ? null : DepartmentId.of(entity.getDepartmentId()),
                entity.getFullName(),
                Email.of(entity.getEmail()),
                entity.getPasswordHash(),
                PhoneNumber.of(entity.getPhone()),
                entity.getRole() == null ? UserRole.CANDIDATE : entity.getRole(),
                entity.getAuthProvider() == null ? AuthProvider.LOCAL : entity.getAuthProvider(),
                entity.getExternalSubjectId(),
                entity.getStatus() == null ? UserStatus.INACTIVE : entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedBy(),
                entity.isDeleted(),
                entity.getDeletedAt()
        );
    }
}
