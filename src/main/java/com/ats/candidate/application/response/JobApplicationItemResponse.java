package com.ats.candidate.application.response;

import java.time.Instant;

/** An application in a job's list. */
public record JobApplicationItemResponse(
        Long id,
        String code,
        Long candidateId,
        String candidateName,
        String candidateEmail,
        String candidatePhone,
        String status,
        String source,
        Instant appliedAt,
        Long cvId,
        String cvParseStatus
) {
}
