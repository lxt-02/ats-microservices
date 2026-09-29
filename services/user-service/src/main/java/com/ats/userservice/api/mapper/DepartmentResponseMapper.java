package com.ats.userservice.api.mapper;

import com.ats.userservice.api.dto.response.DepartmentResponse;
import com.ats.userservice.domain.model.department.aggregate.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentResponseMapper {

    public DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
                department.getId() == null ? null : department.getId().value(),
                department.getParentId() == null ? null : department.getParentId().value(),
                department.getName(),
                department.getDescription(),
                department.getCreatedAt(),
                department.getUpdatedAt(),
                department.getCreatedBy(),
                department.getUpdatedBy()
        );
    }
}
