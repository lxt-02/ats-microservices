package com.ats.candidate.api.controller;

import com.ats.candidate.api.request.UpdatePipelineStageRequest;
import com.ats.candidate.application.response.PipelineBoardResponse;
import com.ats.candidate.application.response.PipelineStageResponse;
import com.ats.candidate.application.usecase.GetPipelineBoardUseCase;
import com.ats.candidate.application.usecase.ManagePipelineStagesUseCase;
import com.ats.shared.security.CurrentUser;
import com.ats.shared.security.RequiresRole;
import com.ats.shared.security.Roles;
import com.ats.shared.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Kanban board of a job and the Kanban stage settings (UC-04.3). */
@RestController
@RequestMapping("/api/v1/pipeline")
@RequiresRole({Roles.ADMIN, Roles.RECRUITER, Roles.HIRING_MANAGER})
public class PipelineController {

    private static final int OK = HttpStatus.OK.value();

    private final GetPipelineBoardUseCase getPipelineBoardUseCase;
    private final ManagePipelineStagesUseCase managePipelineStagesUseCase;

    public PipelineController(GetPipelineBoardUseCase getPipelineBoardUseCase,
                              ManagePipelineStagesUseCase managePipelineStagesUseCase) {
        this.getPipelineBoardUseCase = getPipelineBoardUseCase;
        this.managePipelineStagesUseCase = managePipelineStagesUseCase;
    }

    @GetMapping("/board")
    public ApiResponse<PipelineBoardResponse> board(@RequestParam Long jobId, CurrentUser user) {
        return ApiResponse.success(OK, getPipelineBoardUseCase.execute(jobId, user));
    }

    @GetMapping("/stages")
    public ApiResponse<List<PipelineStageResponse>> stages() {
        return ApiResponse.success(OK, managePipelineStagesUseCase.list());
    }

    @PutMapping("/stages/{id}")
    @RequiresRole(Roles.ADMIN)
    public ApiResponse<PipelineStageResponse> updateStage(@PathVariable Long id,
                                                          @Valid @RequestBody UpdatePipelineStageRequest request,
                                                          CurrentUser user) {
        return ApiResponse.success(OK, managePipelineStagesUseCase.update(request.toCommand(id), user.actor()));
    }
}
