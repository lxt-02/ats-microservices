package com.ats.job.api.request;

import com.ats.job.application.command.CreateJobCommand;
import com.ats.job.application.command.JobFormCommand;
import com.ats.job.domain.enums.EmploymentType;
import com.ats.job.domain.enums.JobPriority;
import com.ats.job.domain.enums.SalaryCurrency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Requisition form fields (SCR-JOB-001). Format rules are checked here; required fields are only enforced when
 * the job is submitted or published, so a draft may be saved half-filled (title required).
 * {@code recruiterId} is only read when an admin creates a job; a recruiter is always assigned to themself.
 */
public record JobFormRequest(
        @Size(max = 200, message = "Job title must not exceed 200 characters")
        String title,
        Long departmentId,
        EmploymentType employmentType,
        @Size(max = 200, message = "Location must not exceed 200 characters")
        String location,
        @Min(value = 1, message = "Number of positions must be between 1 and 99")
        @Max(value = 99, message = "Number of positions must be between 1 and 99")
        Integer positionsCount,
        @DecimalMin(value = "0", message = "Salary must not be negative")
        @Digits(integer = 13, fraction = 2, message = "Salary accepts numbers with up to 2 decimal places")
        BigDecimal salaryMin,
        @DecimalMin(value = "0", message = "Salary must not be negative")
        @Digits(integer = 13, fraction = 2, message = "Salary accepts numbers with up to 2 decimal places")
        BigDecimal salaryMax,
        SalaryCurrency salaryCurrency,
        @Size(max = 50000, message = "Job description must not exceed 50,000 characters")
        String description,
        Set<Long> skillIds,
        Long hiringManagerId,
        LocalDate deadline,
        JobPriority priority,
        Boolean requiresApproval,
        @Size(max = 150) String utmSource,
        @Size(max = 150) String utmMedium,
        Long recruiterId
) {

    public JobFormCommand toCommand() {
        return new JobFormCommand(title, departmentId, employmentType, location, positionsCount, salaryMin,
                salaryMax, salaryCurrency, description, skillIds, hiringManagerId, deadline, priority,
                requiresApproval, utmSource, utmMedium);
    }

    public CreateJobCommand toCreateCommand() {
        return new CreateJobCommand(toCommand(), recruiterId);
    }
}
