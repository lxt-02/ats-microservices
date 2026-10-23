package com.ats.organization.application.usecase;

import com.ats.organization.application.service.DepartmentLoader;
import com.ats.organization.domain.aggregate.Department;
import com.ats.organization.domain.repository.DepartmentRepository;
import com.ats.organization.domain.valueobject.DepartmentId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Soft delete. */
@Service
public class DeleteDepartmentUseCase {

    private final DepartmentRepository departmentRepository;
    private final DepartmentLoader departmentLoader;

    public DeleteDepartmentUseCase(DepartmentRepository departmentRepository, DepartmentLoader departmentLoader) {
        this.departmentRepository = departmentRepository;
        this.departmentLoader = departmentLoader;
    }

    @Transactional
    public void execute(Long id, String actor) {
        Department department = departmentLoader.load(DepartmentId.of(id));
        department.markDeleted(actor);
        departmentRepository.save(department);
    }
}
