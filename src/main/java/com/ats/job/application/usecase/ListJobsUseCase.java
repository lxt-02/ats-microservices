package com.ats.job.application.usecase;

import com.ats.job.application.contract.JobApplicationCounter;
import com.ats.job.application.port.out.JobReadQuery;
import com.ats.job.application.port.out.JobReadQuery.JobRow;
import com.ats.job.application.query.JobListQuery;
import com.ats.job.application.response.DepartmentRef;
import com.ats.job.application.response.JobListItemResponse;
import com.ats.job.application.service.JobAccessPolicy;
import com.ats.organization.application.contract.DepartmentDirectory;
import com.ats.shared.security.CurrentUser;
import com.ats.shared.web.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;

/** Job Management table, limited to the jobs the user may see. */
@Service
public class ListJobsUseCase {

    private final JobReadQuery jobReadQuery;
    private final JobAccessPolicy accessPolicy;
    private final DepartmentDirectory departmentDirectory;
    private final JobApplicationCounter applicationCounter;

    public ListJobsUseCase(JobReadQuery jobReadQuery, JobAccessPolicy accessPolicy,
                           DepartmentDirectory departmentDirectory, JobApplicationCounter applicationCounter) {
        this.jobReadQuery = jobReadQuery;
        this.accessPolicy = accessPolicy;
        this.departmentDirectory = departmentDirectory;
        this.applicationCounter = applicationCounter;
    }

    @Transactional(readOnly = true)
    public PageResponse<JobListItemResponse> execute(JobListQuery query, CurrentUser user) {
        PageResponse<JobRow> page = jobReadQuery.searchManaged(query, accessPolicy.scopeFor(user));
        Map<Long, String> departments = departmentDirectory.findNames(
                page.items().stream().map(JobRow::departmentId).filter(Objects::nonNull).toList());
        Map<Long, Long> counts = page.items().isEmpty() ? Map.of()
                : applicationCounter.countByJobIds(page.items().stream().map(JobRow::id).toList());
        return page.map(row -> new JobListItemResponse(
                row.id(), row.title(), row.employmentType(),
                row.departmentId() == null ? null : new DepartmentRef(row.departmentId(),
                        departments.get(row.departmentId())),
                row.status(), row.priority(), counts.getOrDefault(row.id(), 0L), row.deadline(),
                row.publishedAt(), row.createdAt(), row.recruiterId(), row.hiringManagerId()));
    }
}
