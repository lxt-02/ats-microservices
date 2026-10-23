package com.ats.candidate.application.response;

import java.time.Instant;

/** An application on the candidate profile. */
public record CandidateApplicationResponse(
        Long id,
        String code,
        Long jobId,
        String jobTitle,
        String status,
        String source,
        Instant appliedAt
) {
}
