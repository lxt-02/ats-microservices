package com.ats.candidate.application.response;

import java.time.Instant;
import java.util.List;

/**
 * One application with its pipeline history and notes. {@code allowedMoves}, {@code canTransfer},
 * {@code canReopen} and {@code canDecide} tell the UI which actions the current user may take.
 */
public record ApplicationDetailResponse(
        Long id,
        String code,
        Long jobId,
        String jobTitle,
        Long candidateId,
        String candidateName,
        String candidateEmail,
        String candidatePhone,
        Long cvId,
        String status,
        String stageName,
        String coverLetter,
        String source,
        String referralCode,
        String utmSource,
        String utmMedium,
        String utmCampaign,
        Instant appliedAt,
        Instant statusChangedAt,
        Long transferredFromId,
        HiringDecisionResponse hiringDecision,
        List<String> allowedMoves,
        boolean canTransfer,
        boolean canReopen,
        boolean canDecide,
        List<TransitionResponse> transitions,
        List<EvaluationNoteResponse> notes
) {
}
