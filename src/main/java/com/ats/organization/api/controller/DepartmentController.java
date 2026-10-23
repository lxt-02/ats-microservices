package com.ats.organization.api.controller;

import com.ats.organization.api.request.CreateDepartmentRequest;
import com.ats.organization.api.request.ListDepartmentRequest;
import com.ats.organization.api.request.UpdateDepartmentRequest;
import com.ats.organization.application.response.DepartmentResponse;
import com.ats.organization.application.usecase.CreateDepartmentUseCase;
import com.ats.organization.application.usecase.DeleteDepartmentUseCase;
import com.ats.organization.application.usecase.GetDepartmentByIdUseCase;
import com.ats.organization.application.usecase.ListDepartmentsUseCase;
import com.ats.organization.application.usecase.UpdateDepartmentUseCase;
import com.ats.shared.security.CurrentUser;
import com.ats.shared.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final CreateDepartmentUseCase createDepartmentUseCase;
    private final GetDepartmentByIdUseCase getDepartmentByIdUseCase;
    private final ListDepartmentsUseCase listDepartmentsUseCase;
    private final UpdateDepartmentUseCase updateDepartmentUseCase;
    private final DeleteDepartmentUseCase deleteDepartmentUseCase;

    public DepartmentController(CreateDepartmentUseCase createDepartmentUseCase,
                                GetDepartmentByIdUseCase getDepartmentByIdUseCase,
                                ListDepartmentsUseCase listDepartmentsUseCase,
                                UpdateDepartmentUseCase updateDepartmentUseCase,
                                DeleteDepartmentUseCase deleteDepartmentUseCase) {
        this.createDepartmentUseCase = createDepartmentUseCase;
        this.getDepartmentByIdUseCase = getDepartmentByIdUseCase;
        this.listDepartmentsUseCase = listDepartmentsUseCase;
        this.updateDepartmentUseCase = updateDepartmentUseCase;
        this.deleteDepartmentUseCase = deleteDepartmentUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponse>> create(@Valid @RequestBody CreateDepartmentRequest request,
                                                                  CurrentUser currentUser) {
        DepartmentResponse response = createDepartmentUseCase.execute(request.toCommand(), currentUser.actor());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(HttpStatus.CREATED.value(), response));
    }

    @GetMapping("/{id}")
    public ApiResponse<DepartmentResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(HttpStatus.OK.value(), getDepartmentByIdUseCase.execute(id));
    }

    @GetMapping
    public ApiResponse<List<DepartmentResponse>> list(ListDepartmentRequest request) {
        return ApiResponse.success(HttpStatus.OK.value(), listDepartmentsUseCase.execute(request.toQuery()));
    }

    @PutMapping("/{id}")
    public ApiResponse<DepartmentResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody UpdateDepartmentRequest request,
                                                  CurrentUser currentUser) {
        return ApiResponse.success(HttpStatus.OK.value(),
                updateDepartmentUseCase.execute(request.toCommand(id), currentUser.actor()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, CurrentUser currentUser) {
        deleteDepartmentUseCase.execute(id, currentUser.actor());
        return ResponseEntity.noContent().build();
    }
}
