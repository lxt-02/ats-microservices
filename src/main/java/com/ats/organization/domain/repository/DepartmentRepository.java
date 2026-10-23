package com.ats.organization.domain.repository;

import com.ats.organization.domain.aggregate.Department;
import com.ats.organization.domain.valueobject.DepartmentId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Finders ignore soft-deleted departments. */
public interface DepartmentRepository {

    Department save(Department department);

    Optional<Department> findById(DepartmentId id);

    /** Children of {@code parentId}, or root departments when it is null. */
    List<Department> findByParentId(DepartmentId parentId);

    List<Department> findAll();

    List<Department> findByIds(Collection<Long> ids);
}
