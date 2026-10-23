package com.ats.organization.infrastructure.persistence.converter;

import com.ats.organization.domain.aggregate.Department;
import com.ats.organization.domain.valueobject.DepartmentId;
import com.ats.organization.infrastructure.persistence.record.DepartmentRecord;

/** Record &lt;-&gt; aggregate. */
public final class DepartmentPersistenceConverter {

    private DepartmentPersistenceConverter() {
    }

    public static DepartmentRecord toRecord(Department department) {
        DepartmentRecord record = new DepartmentRecord();
        record.setId(department.getId() == null ? null : department.getId().value());
        record.setParentId(department.getParentId() == null ? null : department.getParentId().value());
        record.setDepartmentName(department.getName());
        record.setDescription(department.getDescription());
        record.setCreatedAt(department.getCreatedAt());
        record.setUpdatedAt(department.getUpdatedAt());
        record.setCreatedBy(department.getCreatedBy());
        record.setUpdatedBy(department.getUpdatedBy());
        record.setDeleted(department.isDeleted());
        record.setDeletedAt(department.getDeletedAt());
        return record;
    }

    public static Department toAggregate(DepartmentRecord record) {
        return Department.restore(
                DepartmentId.of(record.getId()),
                DepartmentId.ofNullable(record.getParentId()),
                record.getDepartmentName(),
                record.getDescription(),
                record.getCreatedAt(),
                record.getUpdatedAt(),
                record.getCreatedBy(),
                record.getUpdatedBy(),
                record.isDeleted(),
                record.getDeletedAt()
        );
    }
}
