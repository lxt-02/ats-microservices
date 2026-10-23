package com.ats.candidate.infrastructure.persistence.record;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Row of table {@code stage_transitions}. */
@Getter
@Setter
@NoArgsConstructor
public class StageTransitionRecord {

    private Long id;
    private Long applicationId;
    private String fromStatus;
    private String toStatus;
    private String notes;
    private Instant movedAt;
    private String createdBy;
}
