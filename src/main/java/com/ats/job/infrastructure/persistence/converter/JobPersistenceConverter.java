package com.ats.job.infrastructure.persistence.converter;

import com.ats.job.domain.aggregate.Job;
import com.ats.job.domain.enums.EmploymentType;
import com.ats.job.domain.enums.JobPriority;
import com.ats.job.domain.enums.JobStatus;
import com.ats.job.domain.enums.SalaryCurrency;
import com.ats.job.domain.valueobject.JobDetails;
import com.ats.job.domain.valueobject.SalaryRange;
import com.ats.job.infrastructure.persistence.record.JobRecord;

import java.util.Set;

public final class JobPersistenceConverter {

    private JobPersistenceConverter() {
    }

    public static JobRecord toRecord(Job job) {
        JobDetails details = job.getDetails();
        JobRecord record = new JobRecord();
        record.setId(job.getId());
        record.setDepartmentId(details.departmentId());
        record.setRecruiterId(job.getRecruiterId());
        record.setHiringManagerId(details.hiringManagerId());
        record.setTitle(details.title());
        record.setDescription(details.description());
        record.setEmploymentType(details.employmentType().name());
        record.setLocation(details.location());
        record.setPositionsCount(details.positionsCount());
        record.setSalaryMin(details.salary().min());
        record.setSalaryMax(details.salary().max());
        record.setSalaryCurrency(details.salary().currency().name());
        record.setDeadline(details.deadline());
        record.setPriority(details.priority().name());
        record.setRequiresApproval(details.requiresApproval());
        record.setStatus(job.getStatus().name());
        record.setApprovedBy(job.getApprovedBy());
        record.setApprovedAt(job.getApprovedAt());
        record.setRejectionReason(job.getRejectionReason());
        record.setUtmSource(details.utmSource());
        record.setUtmMedium(details.utmMedium());
        record.setPublishedAt(job.getPublishedAt());
        record.setClosedAt(job.getClosedAt());
        record.setCreatedAt(job.getCreatedAt());
        record.setUpdatedAt(job.getUpdatedAt());
        record.setCreatedBy(job.getCreatedBy());
        record.setUpdatedBy(job.getUpdatedBy());
        record.setDeleted(job.isDeleted());
        record.setDeletedAt(job.getDeletedAt());
        return record;
    }

    public static Job toAggregate(JobRecord record, Set<Long> skillIds) {
        // Rows are trusted: they were validated when written, so the description is not sanitized twice
        // in a way that could change it (sanitizing sanitized HTML is idempotent).
        JobDetails details = new JobDetails(
                record.getTitle(),
                record.getDepartmentId(),
                EmploymentType.valueOf(record.getEmploymentType()),
                record.getLocation(),
                record.getPositionsCount() == null ? 1 : record.getPositionsCount(),
                new SalaryRange(record.getSalaryMin(), record.getSalaryMax(),
                        record.getSalaryCurrency() == null ? null : SalaryCurrency.valueOf(record.getSalaryCurrency())),
                record.getDescription(),
                skillIds,
                record.getHiringManagerId(),
                record.getDeadline(),
                record.getPriority() == null ? null : JobPriority.valueOf(record.getPriority()),
                record.isRequiresApproval(),
                record.getUtmSource(),
                record.getUtmMedium());
        return Job.restore(record.getId(), record.getRecruiterId(), details, JobStatus.valueOf(record.getStatus()),
                record.getApprovedBy(), record.getApprovedAt(), record.getRejectionReason(), record.getPublishedAt(),
                record.getClosedAt(), record.getCreatedAt(), record.getUpdatedAt(), record.getCreatedBy(),
                record.getUpdatedBy(), record.isDeleted(), record.getDeletedAt());
    }
}
