package com.ats.job.application.response;

import com.ats.job.domain.aggregate.Job;
import com.ats.job.domain.enums.EmploymentType;
import com.ats.job.domain.enums.JobPriority;
import com.ats.job.domain.enums.JobStatus;
import com.ats.job.domain.enums.SalaryCurrency;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Job detail for the internal portal. */
public record JobResponse(
        Long id,
        String title,
        DepartmentRef department,
        EmploymentType employmentType,
        String location,
        int positionsCount,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        SalaryCurrency salaryCurrency,
        String description,
        List<SkillResponse> skills,
        Long hiringManagerId,
        Long recruiterId,
        LocalDate deadline,
        JobPriority priority,
        boolean requiresApproval,
        JobStatus status,
        boolean readyToPublish,
        Long approvedBy,
        Instant approvedAt,
        String rejectionReason,
        String utmSource,
        String utmMedium,
        Instant publishedAt,
        Instant closedAt,
        long applicationCount,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {

    public static JobResponse from(Job job, String departmentName, List<SkillResponse> skills,
                                   long applicationCount, boolean readyToPublish) {
        var details = job.getDetails();
        return new JobResponse(
                job.getId(),
                details.title(),
                details.departmentId() == null ? null : new DepartmentRef(details.departmentId(), departmentName),
                details.employmentType(),
                details.location(),
                details.positionsCount(),
                details.salary().min(),
                details.salary().max(),
                details.salary().currency(),
                details.description(),
                skills,
                details.hiringManagerId(),
                job.getRecruiterId(),
                details.deadline(),
                details.priority(),
                details.requiresApproval(),
                job.getStatus(),
                readyToPublish,
                job.getApprovedBy(),
                job.getApprovedAt(),
                job.getRejectionReason(),
                details.utmSource(),
                details.utmMedium(),
                job.getPublishedAt(),
                job.getClosedAt(),
                applicationCount,
                job.getCreatedAt(),
                job.getUpdatedAt(),
                job.getCreatedBy(),
                job.getUpdatedBy()
        );
    }
}
