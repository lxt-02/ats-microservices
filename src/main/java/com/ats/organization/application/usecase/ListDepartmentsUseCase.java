package com.ats.organization.application.usecase;

import com.ats.organization.application.query.DepartmentListQuery;
import com.ats.organization.application.response.DepartmentResponse;
import com.ats.organization.domain.repository.DepartmentRepository;
import com.ats.organization.domain.valueobject.DepartmentId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListDepartmentsUseCase {

    private final DepartmentRepository departmentRepository;

    public ListDepartmentsUseCase(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> execute(DepartmentListQuery query) {
        return (query.all()
                ? departmentRepository.findAll()
                : departmentRepository.findByParentId(DepartmentId.ofNullable(query.parentId())))
                .stream()
                .map(DepartmentResponse::from)
                .toList();
    }
}
