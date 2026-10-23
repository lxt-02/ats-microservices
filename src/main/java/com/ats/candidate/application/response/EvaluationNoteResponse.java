package com.ats.candidate.application.response;

import com.ats.candidate.domain.aggregate.EvaluationNote;

import java.time.Instant;

public record EvaluationNoteResponse(Long id, Long authorId, String content, Instant createdAt) {

    public static EvaluationNoteResponse from(EvaluationNote note) {
        return new EvaluationNoteResponse(note.getId(), note.getAuthorId(), note.getContent(), note.getCreatedAt());
    }
}
