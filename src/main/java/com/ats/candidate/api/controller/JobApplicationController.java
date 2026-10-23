package com.ats.candidate.api.controller;

import com.ats.candidate.api.request.HmDecisionRequest;
import com.ats.candidate.api.request.ListJobApplicationRequest;
import com.ats.candidate.api.request.MoveApplicationRequest;
import com.ats.candidate.api.request.NoteRequest;
import com.ats.candidate.api.request.OfferOutcomeRequest;
import com.ats.candidate.api.request.ReopenApplicationRequest;
import com.ats.candidate.api.request.TransferApplicationRequest;
import com.ats.candidate.application.response.ApplicationDetailResponse;
import com.ats.candidate.application.response.AssignmentResponse;
import com.ats.candidate.application.response.EvaluationNoteResponse;
import com.ats.candidate.application.response.JobApplicationItemResponse;
import com.ats.candidate.application.response.TransferResponse;
import com.ats.candidate.application.usecase.AddEvaluationNoteUseCase;
import com.ats.candidate.application.usecase.DecideAfterHmInterviewUseCase;
import com.ats.candidate.application.usecase.GetApplicationDetailUseCase;
import com.ats.candidate.application.usecase.ListAssignmentsUseCase;
import com.ats.candidate.application.usecase.ListJobApplicationsUseCase;
import com.ats.candidate.application.usecase.MoveApplicationUseCase;
import com.ats.candidate.application.usecase.RecordOfferOutcomeUseCase;
import com.ats.candidate.application.usecase.ReopenApplicationUseCase;
import com.ats.candidate.application.usecase.TransferApplicationUseCase;
import com.ats.shared.security.CurrentUser;
import com.ats.shared.security.RequiresRole;
import com.ats.shared.security.Roles;
import com.ats.shared.web.ApiResponse;
import com.ats.shared.web.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Applications and the recruitment pipeline. Moves are made by the job's recruiter, HM decisions by the job's
 * hiring manager (checked in the use cases).
 */
@RestController
@RequestMapping("/api/v1/applications")
@RequiresRole({Roles.ADMIN, Roles.RECRUITER, Roles.HIRING_MANAGER})
public class JobApplicationController {

    private static final int OK = HttpStatus.OK.value();

    private final ListJobApplicationsUseCase listJobApplicationsUseCase;
    private final GetApplicationDetailUseCase getApplicationDetailUseCase;
    private final MoveApplicationUseCase moveApplicationUseCase;
    private final ReopenApplicationUseCase reopenApplicationUseCase;
    private final TransferApplicationUseCase transferApplicationUseCase;
    private final DecideAfterHmInterviewUseCase decideAfterHmInterviewUseCase;
    private final RecordOfferOutcomeUseCase recordOfferOutcomeUseCase;
    private final AddEvaluationNoteUseCase addEvaluationNoteUseCase;
    private final ListAssignmentsUseCase listAssignmentsUseCase;

    public JobApplicationController(ListJobApplicationsUseCase listJobApplicationsUseCase,
                                    GetApplicationDetailUseCase getApplicationDetailUseCase,
                                    MoveApplicationUseCase moveApplicationUseCase,
                                    ReopenApplicationUseCase reopenApplicationUseCase,
                                    TransferApplicationUseCase transferApplicationUseCase,
                                    DecideAfterHmInterviewUseCase decideAfterHmInterviewUseCase,
                                    RecordOfferOutcomeUseCase recordOfferOutcomeUseCase,
                                    AddEvaluationNoteUseCase addEvaluationNoteUseCase,
                                    ListAssignmentsUseCase listAssignmentsUseCase) {
        this.listJobApplicationsUseCase = listJobApplicationsUseCase;
        this.getApplicationDetailUseCase = getApplicationDetailUseCase;
        this.moveApplicationUseCase = moveApplicationUseCase;
        this.reopenApplicationUseCase = reopenApplicationUseCase;
        this.transferApplicationUseCase = transferApplicationUseCase;
        this.decideAfterHmInterviewUseCase = decideAfterHmInterviewUseCase;
        this.recordOfferOutcomeUseCase = recordOfferOutcomeUseCase;
        this.addEvaluationNoteUseCase = addEvaluationNoteUseCase;
        this.listAssignmentsUseCase = listAssignmentsUseCase;
    }

    @GetMapping
    public ApiResponse<PageResponse<JobApplicationItemResponse>> list(@Valid ListJobApplicationRequest request,
                                                                      CurrentUser user) {
        return ApiResponse.success(OK, listJobApplicationsUseCase.execute(request.jobId(),
                request.pageOrDefault(), request.sizeOrDefault(), user));
    }

    /** UC-02.16: candidates approved by a hiring manager, with target team, approver and date. */
    @GetMapping("/assignments")
    public ApiResponse<List<AssignmentResponse>> assignments(@RequestParam(required = false) Long jobId,
                                                             CurrentUser user) {
        return ApiResponse.success(OK, listAssignmentsUseCase.execute(jobId, user));
    }

    @GetMapping("/{id}")
    public ApiResponse<ApplicationDetailResponse> get(@PathVariable Long id, CurrentUser user) {
        return ApiResponse.success(OK, getApplicationDetailUseCase.execute(id, user));
    }

    /** UC-02.7 / UC-02.9: move to the next stage, or reject (MSG-PIPE-003 is confirmed on screen). */
    @PostMapping("/{id}/move")
    @RequiresRole({Roles.ADMIN, Roles.RECRUITER})
    public ApiResponse<ApplicationDetailResponse> move(@PathVariable Long id,
                                                       @Valid @RequestBody MoveApplicationRequest request,
                                                       CurrentUser user) {
        return ApiResponse.success(OK, moveApplicationUseCase.execute(request.toCommand(id), user));
    }

    @PostMapping("/{id}/reopen")
    @RequiresRole({Roles.ADMIN, Roles.RECRUITER})
    public ApiResponse<ApplicationDetailResponse> reopen(@PathVariable Long id,
                                                         @Valid @RequestBody(required = false)
                                                         ReopenApplicationRequest request,
                                                         CurrentUser user) {
        return ApiResponse.success(OK, reopenApplicationUseCase.execute(id, request == null ? null : request.note(),
                user));
    }

    /** UC-02.10 (BRULE-04). MSG-PIPE-001 on success, MSG-PIPE-002 when blocked. */
    @PostMapping("/{id}/transfer")
    @RequiresRole({Roles.ADMIN, Roles.RECRUITER})
    public ApiResponse<TransferResponse> transfer(@PathVariable Long id,
                                                  @Valid @RequestBody TransferApplicationRequest request,
                                                  CurrentUser user) {
        TransferResponse response = transferApplicationUseCase.execute(request.toCommand(id), user);
        return ApiResponse.success(OK, response.message(), response);
    }

    /** UC-03.3 Approve &amp; Assign / UC-03.4 Reject, by the job's hiring manager. */
    @PostMapping("/{id}/hm-decision")
    @RequiresRole(Roles.HIRING_MANAGER)
    public ApiResponse<ApplicationDetailResponse> hmDecision(@PathVariable Long id,
                                                             @Valid @RequestBody HmDecisionRequest request,
                                                             CurrentUser user) {
        return ApiResponse.success(OK, decideAfterHmInterviewUseCase.execute(request.toCommand(id), user));
    }

    /** UC-02.18 Update Offer Status. */
    @PostMapping("/{id}/offer-outcome")
    @RequiresRole({Roles.ADMIN, Roles.RECRUITER})
    public ApiResponse<ApplicationDetailResponse> offerOutcome(@PathVariable Long id,
                                                               @Valid @RequestBody OfferOutcomeRequest request,
                                                               CurrentUser user) {
        return ApiResponse.success(OK, recordOfferOutcomeUseCase.execute(request.toCommand(id), user));
    }

    @GetMapping("/{id}/notes")
    public ApiResponse<List<EvaluationNoteResponse>> notes(@PathVariable Long id, CurrentUser user) {
        return ApiResponse.success(OK, addEvaluationNoteUseCase.list(id, user));
    }

    /** UC-02.8 Add Evaluation Notes. */
    @PostMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<EvaluationNoteResponse>> addNote(@PathVariable Long id,
                                                                       @Valid @RequestBody NoteRequest request,
                                                                       CurrentUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(HttpStatus.CREATED.value(),
                addEvaluationNoteUseCase.execute(id, request.content(), user)));
    }
}
