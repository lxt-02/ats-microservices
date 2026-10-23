package com.ats.job.application.contract;

import java.time.LocalDate;

/** Read-only view of a job for other modules. {@code status} is a JobStatus name. */
public record JobInfo(
        Long id,
        String title,
        Long departmentId,
        String status,
        Long recruiterId,
        Long hiringManagerId,
        LocalDate deadline,
        boolean acceptingApplications
) {
}
