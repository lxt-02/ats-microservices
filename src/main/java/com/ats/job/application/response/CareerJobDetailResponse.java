package com.ats.job.application.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Public job detail. Salary fields are null when not disclosed. {@code deadlineApproaching} is true within the
 * last 3 days before the deadline.
 */
public record CareerJobDetailResponse(
        Long id,
        String title,
        DepartmentRef department,
        String location,
        String employmentType,
        String description,
        List<String> skills,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        String salaryCurrency,
        LocalDate deadline,
        boolean deadlineApproaching,
        boolean acceptingApplications,
        Instant publishedAt
) {
}
