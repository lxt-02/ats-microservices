package com.ats.candidate.application.event;

import java.time.Instant;

/** A candidate was moved to another job (non-UI function 10): notify the target job's recruiter. */
public record CandidateTransferredEvent(
        Long candidateId,
        Long sourceApplicationId,
        Long sourceJobId,
        Long targetApplicationId,
        Long targetJobId,
        String transferredBy,
        Instant transferredAt
) {
}
