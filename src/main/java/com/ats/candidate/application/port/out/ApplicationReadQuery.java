package com.ats.candidate.application.port.out;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

/** Screen queries of the recruitment pipeline. */
public interface ApplicationReadQuery {

    /** Every application of the job (Kanban cards). */
    List<BoardCardRow> findBoardCards(Long jobId);

    /** Applications approved by a hiring manager (UC-02.16) for the given jobs, newest decision first. */
    List<AssignmentRow> findApprovedAssignments(Collection<Long> jobIds);

    record BoardCardRow(
            Long applicationId,
            Long candidateId,
            String candidateName,
            String candidateEmail,
            String status,
            Instant appliedAt,
            Instant statusChangedAt,
            String cvParseStatus,
            long notesCount,
            Long transferredFromId
    ) {
    }

    record AssignmentRow(
            Long applicationId,
            Long jobId,
            Long candidateId,
            String candidateName,
            String status,
            Instant appliedAt,
            Long assignedDepartmentId,
            Long decidedBy,
            Instant decidedAt,
            String decisionComment
    ) {
    }
}
