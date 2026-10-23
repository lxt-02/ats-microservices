package com.ats.candidate.api.controller;

import com.ats.candidate.api.request.ListJobApplicationRequest;
import com.ats.candidate.application.response.JobApplicationItemResponse;
import com.ats.candidate.application.usecase.ListJobApplicationsUseCase;
import com.ats.shared.security.CurrentUser;
import com.ats.shared.security.RequiresRole;
import com.ats.shared.security.Roles;
import com.ats.shared.web.ApiResponse;
import com.ats.shared.web.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/applications")
@RequiresRole({Roles.ADMIN, Roles.RECRUITER, Roles.HIRING_MANAGER})
public class JobApplicationController {

    private final ListJobApplicationsUseCase listJobApplicationsUseCase;

    public JobApplicationController(ListJobApplicationsUseCase listJobApplicationsUseCase) {
        this.listJobApplicationsUseCase = listJobApplicationsUseCase;
    }

    @GetMapping
    public ApiResponse<PageResponse<JobApplicationItemResponse>> list(@Valid ListJobApplicationRequest request,
                                                                      CurrentUser user) {
        return ApiResponse.success(HttpStatus.OK.value(), listJobApplicationsUseCase.execute(request.jobId(),
                request.pageOrDefault(), request.sizeOrDefault(), user));
    }
}
