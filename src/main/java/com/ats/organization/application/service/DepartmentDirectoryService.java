package com.ats.organization.application.service;

import com.ats.organization.application.contract.DepartmentDirectory;
import com.ats.organization.domain.aggregate.Department;
import com.ats.organization.domain.repository.DepartmentRepository;
import com.ats.organization.domain.valueobject.DepartmentId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Override
    @Transactional(readOnly = true)
    public Map<Long, String> findNames(Collection<Long> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return Map.of();
        }
        return departmentRepository.findByIds(departmentIds.stream().filter(Objects::nonNull).distinct().toList())
                .stream()
                .collect(Collectors.toMap(department -> department.getId().value(), Department::getName));
    }
}
