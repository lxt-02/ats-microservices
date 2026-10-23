package com.ats.candidate.application.event;

import java.time.Instant;

/** A candidate applied to a job (Career Site or recruiter import). Triggers the auto-reply email (UC-01.3). */
public record ApplicationSubmittedEvent(
        Long applicationId,
        String applicationCode,
        Long jobId,
        String jobTitle,
        Long candidateId,
        String candidateName,
        String candidateEmail,
        Instant appliedAt
) {
}
