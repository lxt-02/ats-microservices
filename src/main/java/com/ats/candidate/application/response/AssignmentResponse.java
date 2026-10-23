package com.ats.candidate.application.response;

import java.time.Instant;

/** UC-02.16: a candidate approved by a hiring manager, with the target team. */
public record AssignmentResponse(
        Long applicationId,
        String code,
        Long jobId,
        String jobTitle,
        Long candidateId,
        String candidateName,
        String status,
        TeamRef team,
        Long approvedBy,
        Instant approvedAt,
        String comment
) {
}
