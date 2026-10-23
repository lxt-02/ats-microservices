package com.ats.candidate.application.response;

import java.time.Instant;

/** Confirmation page data (MSG-APP-001). */
public record SubmittedApplicationResponse(
        Long applicationId,
        String applicationCode,
        Long jobId,
        String jobTitle,
        String status,
        Instant submittedAt
) {
}
