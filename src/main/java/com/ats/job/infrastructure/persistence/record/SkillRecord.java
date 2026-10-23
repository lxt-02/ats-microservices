package com.ats.job.infrastructure.persistence.record;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Row of table {@code skills}. */
@Getter
@Setter
@NoArgsConstructor
public class SkillRecord {

    private Long id;
    private String skillName;
    private String category;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
    private boolean deleted;
}
