package com.ats.userservice.application.service;

import com.ats.userservice.application.command.department.CreateDepartmentCommand;
import com.ats.userservice.application.command.department.UpdateDepartmentCommand;
import com.ats.userservice.application.exception.ResourceNotFoundException;
import com.ats.userservice.application.port.in.DepartmentUseCase;
import com.ats.userservice.domain.model.department.aggregate.Department;
import com.ats.userservice.domain.model.department.valueobject.DepartmentId;
import com.ats.userservice.domain.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DepartmentManagementService implements DepartmentUseCase {

    private final DepartmentRepository departmentRepository;

    public DepartmentManagementService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public Department create(CreateDepartmentCommand command) {
        DepartmentId parentId = toDepartmentId(command.parentId());
        ensureDepartmentExists(parentId);

        Department department = Department.create(parentId, command.name(), command.description(), command.createdBy());
        return departmentRepository.save(department);
    }

    @Override
    @Transactional(readOnly = true)
    public Department getById(Long id) {
        return findDepartment(DepartmentId.of(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Department> getChildren(Long parentId) {
        return departmentRepository.findByParentId(toDepartmentId(parentId));
    }

    @Override
    public Department update(UpdateDepartmentCommand command) {
        Department department = findDepartment(DepartmentId.of(command.id()));
        DepartmentId parentId = toDepartmentId(command.parentId());
        ensureDepartmentExists(parentId);

        department.rename(command.name(), command.updatedBy());
        department.changeParent(parentId, command.updatedBy());
        department.changeDescription(command.description(), command.updatedBy());
        return departmentRepository.save(department);
    }

    @Override
    public void delete(Long id, String updatedBy) {
        Department department = findDepartment(DepartmentId.of(id));
        department.markDeleted(updatedBy);
        departmentRepository.save(department);
    }

    private Department findDepartment(DepartmentId id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id.value()));
    }

    private void ensureDepartmentExists(DepartmentId id) {
        if (id == null) {
            return;
        }
        findDepartment(id);
    }

    private DepartmentId toDepartmentId(Long value) {
        return value == null ? null : DepartmentId.of(value);
    }
}
