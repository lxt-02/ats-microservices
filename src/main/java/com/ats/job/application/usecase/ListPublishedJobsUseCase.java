package com.ats.job.application.usecase;

import com.ats.job.application.contract.SkillInfo;
import com.ats.job.application.port.out.JobReadQuery;
import com.ats.job.application.port.out.JobReadQuery.JobRow;
import com.ats.job.application.query.CareerJobQuery;
import com.ats.job.application.response.CareerJobItemResponse;
import com.ats.job.application.response.DepartmentRef;
import com.ats.job.application.service.BusinessClock;
import com.ats.organization.application.contract.DepartmentDirectory;
import com.ats.shared.security.HtmlSanitizer;
import com.ats.shared.web.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** UC-01.1 Career Site job listing (public). */
@Service
public class ListPublishedJobsUseCase {

    private static final int EXCERPT_LENGTH = 120;

    private final JobReadQuery jobReadQuery;
    private final DepartmentDirectory departmentDirectory;
    private final BusinessClock clock;

    public ListPublishedJobsUseCase(JobReadQuery jobReadQuery, DepartmentDirectory departmentDirectory,
                                    BusinessClock clock) {
        this.jobReadQuery = jobReadQuery;
        this.departmentDirectory = departmentDirectory;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<CareerJobItemResponse> execute(CareerJobQuery query) {
        PageResponse<JobRow> page = jobReadQuery.searchPublished(query, clock.today());
        List<Long> ids = page.items().stream().map(JobRow::id).toList();
        Map<Long, List<SkillInfo>> skills = jobReadQuery.findSkillsByJobIds(ids);
        Map<Long, String> departments = departmentDirectory.findNames(
                page.items().stream().map(JobRow::departmentId).filter(Objects::nonNull).toList());
        return page.map(row -> new CareerJobItemResponse(
                row.id(), row.title(),
                new DepartmentRef(row.departmentId(), departments.get(row.departmentId())),
                row.location(), row.employmentType(),
                HtmlSanitizer.excerpt(row.description(), EXCERPT_LENGTH),
                skills.getOrDefault(row.id(), List.of()).stream().map(SkillInfo::name).toList(),
                row.deadline(), row.publishedAt()));
    }
}
