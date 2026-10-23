package com.ats.job.application.command;

import com.ats.job.domain.enums.EmploymentType;
import com.ats.job.domain.enums.JobPriority;
import com.ats.job.domain.enums.SalaryCurrency;
import com.ats.job.domain.valueobject.JobDetails;
import com.ats.job.domain.valueobject.SalaryRange;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/** The requisition form (create and edit). */
public record JobFormCommand(
        String title,
        Long departmentId,
        EmploymentType employmentType,
        String location,
        Integer positionsCount,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        SalaryCurrency salaryCurrency,
        String description,
        Set<Long> skillIds,
        Long hiringManagerId,
        LocalDate deadline,
        JobPriority priority,
        Boolean requiresApproval,
        String utmSource,
        String utmMedium
) {

    public JobDetails toDetails() {
        return new JobDetails(title, departmentId, employmentType, location,
                positionsCount == null ? 1 : positionsCount,
                new SalaryRange(salaryMin, salaryMax, salaryCurrency),
                description, skillIds, hiringManagerId, deadline, priority,
                requiresApproval == null || requiresApproval, utmSource, utmMedium);
    }
}
