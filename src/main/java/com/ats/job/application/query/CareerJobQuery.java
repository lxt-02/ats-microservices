package com.ats.job.application.query;

import com.ats.job.domain.enums.EmploymentType;

/** Career Site search: keyword matches title, description or skill name. {@code page} is 1-based. */
public record CareerJobQuery(
        String keyword,
        Long departmentId,
        String location,
        EmploymentType employmentType,
        CareerJobSort sort,
        int page,
        int size
) {
}
