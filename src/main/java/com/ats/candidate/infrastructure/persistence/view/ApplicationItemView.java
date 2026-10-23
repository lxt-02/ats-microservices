package com.ats.candidate.infrastructure.persistence.view;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class ApplicationItemView {

    private Long id;
    private Long jobId;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private String candidatePhone;
    private String status;
    private String source;
    private Instant appliedAt;
    private Long cvId;
    private String cvParseStatus;
}
