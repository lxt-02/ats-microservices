package com.ats.candidate.infrastructure.persistence.view;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Row of the candidate search screen. */
@Getter
@Setter
@NoArgsConstructor
public class CandidateItemView {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String source;
    private boolean duplicate;
    private Instant createdAt;
    private Long latestCvId;
    private String latestCvFileName;
    private String latestCvParseStatus;
    private long applicationCount;
}
