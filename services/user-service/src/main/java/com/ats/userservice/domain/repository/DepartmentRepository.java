package com.ats.userservice.domain.repository;

import com.ats.userservice.domain.model.department.aggregate.Department;
import com.ats.userservice.domain.model.department.valueobject.DepartmentId;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository {

    Department save(Department department);

    Optional<Department> findById(DepartmentId id);

    List<Department> findByParentId(DepartmentId parentId);
}
