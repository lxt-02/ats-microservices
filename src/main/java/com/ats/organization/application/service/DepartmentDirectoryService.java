package com.ats.organization.application.service;

import com.ats.organization.application.contract.DepartmentDirectory;
import com.ats.organization.domain.repository.DepartmentRepository;
import com.ats.organization.domain.valueobject.DepartmentId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentDirectoryService implements DepartmentDirectory {

    private final DepartmentRepository departmentRepository;

    public DepartmentDirectoryService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Long departmentId) {
        return departmentId != null && departmentRepository.findById(DepartmentId.of(departmentId)).isPresent();
    }
}
