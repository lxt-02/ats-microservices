package com.ats.candidate.infrastructure.persistence.record;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Row of table {@code evaluation_notes}. */
@Getter
@Setter
@NoArgsConstructor
public class EvaluationNoteRecord {

    private Long id;
    private Long applicationId;
    private Long authorId;
    private String content;
    private Instant createdAt;
    private String createdBy;
}
