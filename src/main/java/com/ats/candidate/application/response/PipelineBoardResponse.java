package com.ats.candidate.application.response;

import java.time.Instant;
import java.util.List;

/** Kanban board of one job: one column per configured stage, in stage order. */
public record PipelineBoardResponse(Long jobId, String jobTitle, long total, List<Column> columns) {

    public record Column(Long stageId, String status, String name, String color, int count, List<Card> cards) {
    }

    public record Card(
            Long applicationId,
            String code,
            Long candidateId,
            String candidateName,
            String candidateEmail,
            Instant appliedAt,
            Instant statusChangedAt,
            String cvParseStatus,
            long notesCount,
            boolean transferred
    ) {
    }
}
