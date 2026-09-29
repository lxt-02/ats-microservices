package com.ats.userservice.infrastructure.mapper;

import com.ats.userservice.domain.model.department.aggregate.Department;
import com.ats.userservice.domain.model.department.valueobject.DepartmentId;
import com.ats.userservice.infrastructure.persistence.entity.DepartmentJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class DepartmentPersistenceMapper {

    public DepartmentJpaEntity toEntity(Department department) {
        DepartmentJpaEntity entity = new DepartmentJpaEntity();
        entity.setId(department.getId() == null ? null : department.getId().value());
        entity.setParentId(department.getParentId() == null ? null : department.getParentId().value());
        entity.setDepartmentName(department.getName());
        entity.setDescription(department.getDescription());
        entity.setCreatedAt(department.getCreatedAt());
        entity.setUpdatedAt(department.getUpdatedAt());
        entity.setCreatedBy(department.getCreatedBy());
        entity.setUpdatedBy(department.getUpdatedBy());
        entity.setDeleted(department.isDeleted());
        entity.setDeletedAt(department.getDeletedAt());
        return entity;
    }

    public Department toDomain(DepartmentJpaEntity entity) {
        return Department.restore(
                DepartmentId.of(entity.getId()),
                entity.getParentId() == null ? null : DepartmentId.of(entity.getParentId()),
                entity.getDepartmentName(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedBy(),
                entity.isDeleted(),
                entity.getDeletedAt()
        );
    }
}
