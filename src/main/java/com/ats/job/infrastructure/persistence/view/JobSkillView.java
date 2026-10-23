package com.ats.job.infrastructure.persistence.view;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A (job, skill) pair, with the skill name when joined. */
@Getter
@Setter
@NoArgsConstructor
public class JobSkillView {

    private Long jobId;
    private Long skillId;
    private String skillName;
    private String category;
}
