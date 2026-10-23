package com.ats.candidate.api.controller;

import com.ats.candidate.api.request.SubmitApplicationRequest;
import com.ats.candidate.application.response.SubmittedApplicationResponse;
import com.ats.candidate.application.usecase.SubmitJobApplicationUseCase;
import com.ats.shared.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public Career Site: UC-01.2 Submit Job Application (no account required). */
@RestController
@RequestMapping("/api/v1/public/jobs/{jobId}/applications")
public class CareerApplicationController {

    private final SubmitJobApplicationUseCase submitJobApplicationUseCase;

    public CareerApplicationController(SubmitJobApplicationUseCase submitJobApplicationUseCase) {
        this.submitJobApplicationUseCase = submitJobApplicationUseCase;
    }

    /** MSG-APP-001 on success. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SubmittedApplicationResponse>> submit(
            @PathVariable Long jobId, @Valid @ModelAttribute SubmitApplicationRequest request) {
        SubmittedApplicationResponse response = submitJobApplicationUseCase.execute(request.toCommand(jobId));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(HttpStatus.CREATED.value(),
                "Your application has been submitted successfully! Application ID: " + response.applicationCode(),
                response));
    }
}
