package com.ats.job.application.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Job card on the Career Site. {@code excerpt}: first 120 characters of the description as plain text. */
public record CareerJobItemResponse(
        Long id,
        String title,
        DepartmentRef department,
        String location,
        String employmentType,
        String excerpt,
        List<String> skills,
        LocalDate deadline,
        Instant publishedAt
) {
}
