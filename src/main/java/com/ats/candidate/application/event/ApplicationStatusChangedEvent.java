package com.ats.candidate.application.event;

import java.time.Instant;

/**
 * A pipeline state change (e.g. to REJECTED: rejection letter, UC-01.5). States are ApplicationStatus names.
 */
public record ApplicationStatusChangedEvent(
        Long applicationId,
        Long jobId,
        Long candidateId,
        String fromStatus,
        String toStatus,
        String note,
        String changedBy,
        Instant changedAt
) {
}
