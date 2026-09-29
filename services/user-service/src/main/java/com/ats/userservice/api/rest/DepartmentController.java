package com.ats.userservice.api.rest;

import com.ats.userservice.api.dto.request.DepartmentRequest;
import com.ats.userservice.api.dto.response.DepartmentResponse;
import com.ats.userservice.api.mapper.DepartmentResponseMapper;
import com.ats.userservice.application.command.department.CreateDepartmentCommand;
import com.ats.userservice.application.command.department.UpdateDepartmentCommand;
import com.ats.userservice.application.port.in.DepartmentUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentUseCase departmentUseCase;
    private final DepartmentResponseMapper departmentResponseMapper;

    public DepartmentController(DepartmentUseCase departmentUseCase, DepartmentResponseMapper departmentResponseMapper) {
        this.departmentUseCase = departmentUseCase;
        this.departmentResponseMapper = departmentResponseMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentResponse create(@Valid @RequestBody DepartmentRequest request) {
        return departmentResponseMapper.toResponse(departmentUseCase.create(new CreateDepartmentCommand(
                request.getParentId(),
                request.getName(),
                request.getDescription(),
                request.getActor()
        )));
    }

    @GetMapping("/{id}")
    public DepartmentResponse getById(@PathVariable Long id) {
        return departmentResponseMapper.toResponse(departmentUseCase.getById(id));
    }

    @GetMapping
    public List<DepartmentResponse> getChildren(@RequestParam(required = false) Long parentId) {
        return departmentUseCase.getChildren(parentId)
                .stream()
                .map(departmentResponseMapper::toResponse)
                .toList();
    }

    @PutMapping("/{id}")
    public DepartmentResponse update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        return departmentResponseMapper.toResponse(departmentUseCase.update(new UpdateDepartmentCommand(
                id,
                request.getParentId(),
                request.getName(),
                request.getDescription(),
                request.getActor()
        )));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @RequestParam(required = false) String actor) {
        departmentUseCase.delete(id, actor);
    }
}
