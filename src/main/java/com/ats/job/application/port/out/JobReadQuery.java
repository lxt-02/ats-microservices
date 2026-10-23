package com.ats.job.application.port.out;

import com.ats.job.application.contract.SkillInfo;
import com.ats.job.application.query.CareerJobQuery;
import com.ats.job.application.query.JobListQuery;
import com.ats.job.application.query.JobScope;
import com.ats.job.application.response.JobStatsResponse;
import com.ats.shared.web.PageResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Screen queries of the Job module (lists, counts, Career Site). */
public interface JobReadQuery {

    PageResponse<JobRow> searchManaged(JobListQuery query, JobScope scope);

    JobStatsResponse countByStatus(JobScope scope);

    /** Published jobs whose deadline has not passed on {@code today}. */
    PageResponse<JobRow> searchPublished(CareerJobQuery query, LocalDate today);

    Map<Long, List<SkillInfo>> findSkillsByJobIds(Collection<Long> jobIds);

    List<Long> findDepartmentIdsOfPublishedJobs(LocalDate today);

    List<String> findLocationsOfPublishedJobs(LocalDate today);

    Set<Long> findJobIdsAssignedTo(Long userId);

    /** One row of a job list. {@code description} is the sanitized HTML. */
    record JobRow(
            Long id,
            String title,
            Long departmentId,
            String employmentType,
            String location,
            String description,
            BigDecimal salaryMin,
            BigDecimal salaryMax,
            String salaryCurrency,
            String status,
            String priority,
            Long recruiterId,
            Long hiringManagerId,
            LocalDate deadline,
            Instant publishedAt,
            Instant createdAt
    ) {
    }
}
