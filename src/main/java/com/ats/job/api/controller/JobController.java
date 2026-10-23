package com.ats.job.api.controller;

import com.ats.job.api.request.BulkJobActionRequest;
import com.ats.job.api.request.JobFormRequest;
import com.ats.job.api.request.ListJobRequest;
import com.ats.job.api.request.RejectJobRequest;
import com.ats.job.api.request.UpdateJobRequest;
import com.ats.job.application.response.BulkJobActionResponse;
import com.ats.job.application.response.JobListItemResponse;
import com.ats.job.application.response.JobResponse;
import com.ats.job.application.response.JobStatsResponse;
import com.ats.job.application.usecase.ApproveJobUseCase;
import com.ats.job.application.usecase.BulkJobActionUseCase;
import com.ats.job.application.usecase.CloseJobUseCase;
import com.ats.job.application.usecase.CreateJobUseCase;
import com.ats.job.application.usecase.DeleteJobUseCase;
import com.ats.job.application.usecase.GetJobStatsUseCase;
import com.ats.job.application.usecase.GetJobUseCase;
import com.ats.job.application.usecase.ListJobsUseCase;
import com.ats.job.application.usecase.PublishJobUseCase;
import com.ats.job.application.usecase.RejectJobUseCase;
import com.ats.job.application.usecase.ReopenJobUseCase;
import com.ats.job.application.usecase.SubmitJobUseCase;
import com.ats.job.application.usecase.UnpublishJobUseCase;
import com.ats.job.application.usecase.UpdateJobUseCase;
import com.ats.shared.security.CurrentUser;
import com.ats.shared.security.RequiresRole;
import com.ats.shared.security.Roles;
import com.ats.shared.web.ApiResponse;
import com.ats.shared.web.PageResponse;
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

/** Job Management (internal portal). Data scope per user is applied in the use cases. */
@RestController
@RequestMapping("/api/v1/jobs")
@RequiresRole({Roles.ADMIN, Roles.RECRUITER})
public class JobController {

    private static final int OK = HttpStatus.OK.value();

    private final CreateJobUseCase createJobUseCase;
    private final UpdateJobUseCase updateJobUseCase;
    private final GetJobUseCase getJobUseCase;
    private final ListJobsUseCase listJobsUseCase;
    private final GetJobStatsUseCase getJobStatsUseCase;
    private final SubmitJobUseCase submitJobUseCase;
    private final ApproveJobUseCase approveJobUseCase;
    private final RejectJobUseCase rejectJobUseCase;
    private final PublishJobUseCase publishJobUseCase;
    private final UnpublishJobUseCase unpublishJobUseCase;
    private final CloseJobUseCase closeJobUseCase;
    private final ReopenJobUseCase reopenJobUseCase;
    private final DeleteJobUseCase deleteJobUseCase;
    private final BulkJobActionUseCase bulkJobActionUseCase;

    public JobController(CreateJobUseCase createJobUseCase, UpdateJobUseCase updateJobUseCase,
                         GetJobUseCase getJobUseCase, ListJobsUseCase listJobsUseCase,
                         GetJobStatsUseCase getJobStatsUseCase, SubmitJobUseCase submitJobUseCase,
                         ApproveJobUseCase approveJobUseCase, RejectJobUseCase rejectJobUseCase,
                         PublishJobUseCase publishJobUseCase, UnpublishJobUseCase unpublishJobUseCase,
                         CloseJobUseCase closeJobUseCase, ReopenJobUseCase reopenJobUseCase,
                         DeleteJobUseCase deleteJobUseCase, BulkJobActionUseCase bulkJobActionUseCase) {
        this.createJobUseCase = createJobUseCase;
        this.updateJobUseCase = updateJobUseCase;
        this.getJobUseCase = getJobUseCase;
        this.listJobsUseCase = listJobsUseCase;
        this.getJobStatsUseCase = getJobStatsUseCase;
        this.submitJobUseCase = submitJobUseCase;
        this.approveJobUseCase = approveJobUseCase;
        this.rejectJobUseCase = rejectJobUseCase;
        this.publishJobUseCase = publishJobUseCase;
        this.unpublishJobUseCase = unpublishJobUseCase;
        this.closeJobUseCase = closeJobUseCase;
        this.reopenJobUseCase = reopenJobUseCase;
        this.deleteJobUseCase = deleteJobUseCase;
        this.bulkJobActionUseCase = bulkJobActionUseCase;
    }

    /** "Save Draft" (MSG-JOB-004). Call {@code POST /{id}/submit} afterwards for "Save & Submit". */
    @PostMapping
    public ResponseEntity<ApiResponse<JobResponse>> create(@Valid @RequestBody JobFormRequest request,
                                                           CurrentUser user) {
        JobResponse response = createJobUseCase.execute(request.toCreateCommand(), user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(HttpStatus.CREATED.value(),
                "Job requisition has been saved as draft.", response));
    }

    @GetMapping
    @RequiresRole({Roles.ADMIN, Roles.RECRUITER, Roles.HIRING_MANAGER})
    public ApiResponse<PageResponse<JobListItemResponse>> list(@Valid ListJobRequest request, CurrentUser user) {
        return ApiResponse.success(OK, listJobsUseCase.execute(request.toQuery(), user));
    }

    @GetMapping("/stats")
    @RequiresRole({Roles.ADMIN, Roles.RECRUITER, Roles.HIRING_MANAGER})
    public ApiResponse<JobStatsResponse> stats(CurrentUser user) {
        return ApiResponse.success(OK, getJobStatsUseCase.execute(user));
    }

    @GetMapping("/{id}")
    @RequiresRole({Roles.ADMIN, Roles.RECRUITER, Roles.HIRING_MANAGER})
    public ApiResponse<JobResponse> get(@PathVariable Long id, CurrentUser user) {
        return ApiResponse.success(OK, getJobUseCase.execute(id, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<JobResponse> update(@PathVariable Long id, @Valid @RequestBody JobFormRequest request,
                                           CurrentUser user) {
        return ApiResponse.success(OK, updateJobUseCase.execute(UpdateJobRequest.toCommand(id, request), user));
    }

    /** MSG-JOB-005 when it goes to the hiring manager. */
    @PostMapping("/{id}/submit")
    public ApiResponse<JobResponse> submit(@PathVariable Long id, CurrentUser user) {
        JobResponse response = submitJobUseCase.execute(id, user);
        String message = response.status().name().equals("PENDING_APPROVAL")
                ? "Job requisition has been submitted for Hiring Manager approval."
                : "Job requisition is ready to publish.";
        return ApiResponse.success(OK, message, response);
    }

    @PostMapping("/{id}/approve")
    @RequiresRole(Roles.HIRING_MANAGER)
    public ApiResponse<JobResponse> approve(@PathVariable Long id, CurrentUser user) {
        return ApiResponse.success(OK, approveJobUseCase.execute(id, user));
    }

    @PostMapping("/{id}/reject")
    @RequiresRole(Roles.HIRING_MANAGER)
    public ApiResponse<JobResponse> reject(@PathVariable Long id, @Valid @RequestBody RejectJobRequest request,
                                           CurrentUser user) {
        return ApiResponse.success(OK, rejectJobUseCase.execute(id, request.reason(), user));
    }

    /** MSG-PUB-003. */
    @PostMapping("/{id}/publish")
    public ApiResponse<JobResponse> publish(@PathVariable Long id, CurrentUser user) {
        return ApiResponse.success(OK, "Job has been published successfully to the Career Site.",
                publishJobUseCase.execute(id, user));
    }

    @PostMapping("/{id}/unpublish")
    public ApiResponse<JobResponse> unpublish(@PathVariable Long id, CurrentUser user) {
        return ApiResponse.success(OK, unpublishJobUseCase.execute(id, user));
    }

    @PostMapping("/{id}/close")
    public ApiResponse<JobResponse> close(@PathVariable Long id, CurrentUser user) {
        return ApiResponse.success(OK, closeJobUseCase.execute(id, user));
    }

    @PostMapping("/{id}/reopen")
    public ApiResponse<JobResponse> reopen(@PathVariable Long id, CurrentUser user) {
        return ApiResponse.success(OK, reopenJobUseCase.execute(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, CurrentUser user) {
        deleteJobUseCase.execute(id, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-actions")
    public ApiResponse<BulkJobActionResponse> bulk(@Valid @RequestBody BulkJobActionRequest request,
                                                   CurrentUser user) {
        return ApiResponse.success(OK, bulkJobActionUseCase.execute(request.action(), request.jobIds(), user));
    }
}
