package com.ats.organization.application.usecase;

import com.ats.organization.application.command.UpdateDepartmentCommand;
import com.ats.organization.application.response.DepartmentResponse;
import com.ats.organization.application.service.DepartmentLoader;
import com.ats.organization.domain.aggregate.Department;
import com.ats.organization.domain.repository.DepartmentRepository;
import com.ats.organization.domain.valueobject.DepartmentId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateDepartmentUseCase {

    private final DepartmentRepository departmentRepository;
    private final DepartmentLoader departmentLoader;

    public UpdateDepartmentUseCase(DepartmentRepository departmentRepository, DepartmentLoader departmentLoader) {
        this.departmentRepository = departmentRepository;
        this.departmentLoader = departmentLoader;
    }

    @Transactional
    public DepartmentResponse execute(UpdateDepartmentCommand command, String actor) {
        Department department = departmentLoader.load(DepartmentId.of(command.id()));
        DepartmentId parentId = DepartmentId.ofNullable(command.parentId());
        departmentLoader.ensureExists(parentId);

        department.rename(command.name(), actor);
        department.changeParent(parentId, actor);
        department.changeDescription(command.description(), actor);
        return DepartmentResponse.from(departmentRepository.save(department));
    }
}
