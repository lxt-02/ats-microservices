package com.ats.job.api.controller;

import com.ats.job.api.request.CreateSkillRequest;
import com.ats.job.application.response.SkillResponse;
import com.ats.job.application.usecase.CreateSkillUseCase;
import com.ats.job.application.usecase.ListSkillsUseCase;
import com.ats.shared.security.CurrentUser;
import com.ats.shared.security.RequiresRole;
import com.ats.shared.security.Roles;
import com.ats.shared.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Skill master data: read by every internal user (autocomplete), maintained by admins. */
@RestController
@RequestMapping("/api/v1/skills")
public class SkillController {

    private final ListSkillsUseCase listSkillsUseCase;
    private final CreateSkillUseCase createSkillUseCase;

    public SkillController(ListSkillsUseCase listSkillsUseCase, CreateSkillUseCase createSkillUseCase) {
        this.listSkillsUseCase = listSkillsUseCase;
        this.createSkillUseCase = createSkillUseCase;
    }

    @GetMapping
    @RequiresRole({Roles.ADMIN, Roles.RECRUITER, Roles.HIRING_MANAGER})
    public ApiResponse<List<SkillResponse>> list(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(HttpStatus.OK.value(), listSkillsUseCase.execute(keyword));
    }

    @PostMapping
    @RequiresRole(Roles.ADMIN)
    public ResponseEntity<ApiResponse<SkillResponse>> create(@Valid @RequestBody CreateSkillRequest request,
                                                             CurrentUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(HttpStatus.CREATED.value(),
                createSkillUseCase.execute(request.toCommand(), user.actor())));
    }
}
