package com.ats.userservice.application.port.in;

import com.ats.userservice.application.command.department.CreateDepartmentCommand;
import com.ats.userservice.application.command.department.UpdateDepartmentCommand;
import com.ats.userservice.domain.model.department.aggregate.Department;

import java.util.List;

public interface DepartmentUseCase {

    Department create(CreateDepartmentCommand command);

    Department getById(Long id);

    List<Department> getChildren(Long parentId);

    Department update(UpdateDepartmentCommand command);

    void delete(Long id, String updatedBy);
}
