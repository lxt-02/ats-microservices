package com.ats.job.domain.valueobject;

import com.ats.job.domain.enums.EmploymentType;
import com.ats.job.domain.enums.JobPriority;
import com.ats.shared.security.HtmlSanitizer;

import java.time.LocalDate;
import java.util.Set;

/**
 * Everything a recruiter edits on the requisition form. Fields may be empty while the job is a draft;
 * completeness is checked when it is submitted or published.
 *
 * @param description rich text (HTML); always stored sanitized (it is shown on the public Career Site)
 */
public record JobDetails(
        String title,
        Long departmentId,
        EmploymentType employmentType,
        String location,
        int positionsCount,
        SalaryRange salary,
        String description,
        Set<Long> skillIds,
        Long hiringManagerId,
        LocalDate deadline,
        JobPriority priority,
        boolean requiresApproval,
        String utmSource,
        String utmMedium
) {

    public JobDetails {
        title = blankToNull(title);
        employmentType = employmentType == null ? EmploymentType.FULL_TIME : employmentType;
        location = blankToNull(location);
        if (positionsCount < 1 || positionsCount > 99) {
            throw new IllegalArgumentException("Number of positions must be between 1 and 99");
        }
        salary = salary == null ? SalaryRange.none() : salary;
        description = HtmlSanitizer.sanitize(blankToNull(description));
        skillIds = skillIds == null ? Set.of() : Set.copyOf(skillIds);
        priority = priority == null ? JobPriority.NORMAL : priority;
        utmSource = blankToNull(utmSource);
        utmMedium = blankToNull(utmMedium);
    }

    public JobDetails withTitle(String newTitle) {
        return new JobDetails(newTitle, departmentId, employmentType, location, positionsCount, salary, description,
                skillIds, hiringManagerId, deadline, priority, requiresApproval, utmSource, utmMedium);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
