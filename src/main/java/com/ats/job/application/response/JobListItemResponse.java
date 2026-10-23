package com.ats.job.application.response;

import java.time.Instant;
import java.time.LocalDate;

/** Row of the Job Management table. */
public record JobListItemResponse(
        Long id,
        String title,
        String employmentType,
        DepartmentRef department,
        String status,
        String priority,
        long applicationCount,
        LocalDate deadline,
        Instant publishedAt,
        Instant createdAt,
        Long recruiterId,
        Long hiringManagerId
) {
}
