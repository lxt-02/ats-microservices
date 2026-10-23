package com.ats.organization.application.usecase;

import com.ats.organization.application.response.DepartmentResponse;
import com.ats.organization.application.service.DepartmentLoader;
import com.ats.organization.domain.valueobject.DepartmentId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetDepartmentByIdUseCase {

    private final DepartmentLoader departmentLoader;

    public GetDepartmentByIdUseCase(DepartmentLoader departmentLoader) {
        this.departmentLoader = departmentLoader;
    }

    @Transactional(readOnly = true)
    public DepartmentResponse execute(Long id) {
        return DepartmentResponse.from(departmentLoader.load(DepartmentId.of(id)));
    }
}
