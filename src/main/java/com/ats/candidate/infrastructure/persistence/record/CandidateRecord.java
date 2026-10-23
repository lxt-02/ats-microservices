package com.ats.candidate.infrastructure.persistence.record;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Row of table {@code candidates}. Skills are rows of {@code candidate_skills}. */
@Getter
@Setter
@NoArgsConstructor
public class CandidateRecord {

    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    /** Last 9 digits of the phone, to match "+84 912 345 678" with "0912345678". */
    private String phoneKey;
    private String status;
    private String source;
    private String utmSource;
    private String utmMedium;
    private String utmCampaign;
    private boolean duplicate;
    private Long duplicateOfId;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private boolean deleted;
    private Instant deletedAt;
}
