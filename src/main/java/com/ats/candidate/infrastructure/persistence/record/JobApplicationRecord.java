package com.ats.candidate.infrastructure.persistence.record;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Row of table {@code applications}. */
@Getter
@Setter
@NoArgsConstructor
public class JobApplicationRecord {

    private Long id;
    private Long jobId;
    private Long candidateId;
    private Long cvId;
    private String status;
    private String coverLetter;
    private String source;
    private String referralCode;
    private String utmSource;
    private String utmMedium;
    private String utmCampaign;
    private Instant appliedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
