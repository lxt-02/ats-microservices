package com.ats.organization.domain.repository;

import com.ats.organization.domain.aggregate.Department;
import com.ats.organization.domain.valueobject.DepartmentId;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository {

    Department save(Department department);

    /** Finds a department that is not deleted. */
    Optional<Department> findById(DepartmentId id);

    /** Children of {@code parentId}, or root departments when it is null. */
    List<Department> findByParentId(DepartmentId parentId);
}
