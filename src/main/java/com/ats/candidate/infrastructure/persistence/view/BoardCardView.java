package com.ats.candidate.infrastructure.persistence.view;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class BoardCardView {

    private Long applicationId;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String status;
    private Instant appliedAt;
    private Instant statusChangedAt;
    private String cvParseStatus;
    private long notesCount;
    private Long transferredFromId;
}
