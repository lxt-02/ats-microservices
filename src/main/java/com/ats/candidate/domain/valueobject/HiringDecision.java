package com.ats.candidate.domain.valueobject;

import java.time.Instant;

/** Hiring Manager decision after the HM interview (UC-03.3 / UC-03.4); team is set when approved. */
public record HiringDecision(boolean approved, Long assignedDepartmentId, String comment, Long decidedBy,
                             Instant decidedAt) {
}
