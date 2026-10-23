package com.ats.organization.application.usecase;

import com.ats.organization.application.command.CreateDepartmentCommand;
import com.ats.organization.application.response.DepartmentResponse;
import com.ats.organization.application.service.DepartmentLoader;
import com.ats.organization.domain.aggregate.Department;
import com.ats.organization.domain.repository.DepartmentRepository;
import com.ats.organization.domain.valueobject.DepartmentId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateDepartmentUseCase {

    private final DepartmentRepository departmentRepository;
    private final DepartmentLoader departmentLoader;

    public CreateDepartmentUseCase(DepartmentRepository departmentRepository, DepartmentLoader departmentLoader) {
        this.departmentRepository = departmentRepository;
        this.departmentLoader = departmentLoader;
    }

    @Transactional
    public DepartmentResponse execute(CreateDepartmentCommand command, String actor) {
        DepartmentId parentId = DepartmentId.ofNullable(command.parentId());
        departmentLoader.ensureExists(parentId);

        Department department = Department.create(parentId, command.name(), command.description(), actor);
        return DepartmentResponse.from(departmentRepository.save(department));
    }
}
