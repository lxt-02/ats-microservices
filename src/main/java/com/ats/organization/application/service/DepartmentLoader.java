package com.ats.organization.application.service;

import com.ats.organization.domain.aggregate.Department;
import com.ats.organization.domain.repository.DepartmentRepository;
import com.ats.organization.domain.valueobject.DepartmentId;
import com.ats.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

/** Loads departments for the use cases, failing with 404. */
@Service
public class DepartmentLoader {

    private final DepartmentRepository departmentRepository;

    public DepartmentLoader(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public Department load(DepartmentId id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id.value()));
    }

    /** Checks an optional parent reference. */
    public void ensureExists(DepartmentId id) {
        if (id != null) {
            load(id);
        }
    }
}
