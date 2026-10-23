package com.ats.job.infrastructure.persistence.query;

import com.ats.job.application.contract.SkillInfo;
import com.ats.job.application.port.out.JobReadQuery;
import com.ats.job.application.query.CareerJobQuery;
import com.ats.job.application.query.JobListQuery;
import com.ats.job.application.query.JobScope;
import com.ats.job.application.response.JobStatsResponse;
import com.ats.job.domain.enums.JobStatus;
import com.ats.job.infrastructure.persistence.mapper.JobReadMapper;
import com.ats.job.infrastructure.persistence.view.JobItemView;
import com.ats.job.infrastructure.persistence.view.JobSkillView;
import com.ats.shared.infrastructure.mybatis.LikePatterns;
import com.ats.shared.web.PageResponse;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class MyBatisJobReadQuery implements JobReadQuery {

    /** Whitelisted ORDER BY clauses; user input never reaches the SQL text. */
    private static final Map<String, String> MANAGED_SORT_COLUMNS = Map.of(
            "createdAt", "created_at",
            "title", "title",
            "publishedAt", "published_at",
            "deadline", "deadline");

    private final JobReadMapper mapper;

    public MyBatisJobReadQuery(JobReadMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public PageResponse<JobRow> searchManaged(JobListQuery query, JobScope scope) {
        String keyword = LikePatterns.containsLower(query.keyword());
        Long keywordId = parseId(query.keyword());
        String status = query.status() == null ? null : query.status().name();
        long total = mapper.countManaged(keyword, keywordId, status, scope.recruiterId(), scope.hiringManagerId());
        String column = query.sortKey() == null ? "created_at"
                : MANAGED_SORT_COLUMNS.getOrDefault(query.sortKey(), "created_at");
        String orderBy = column + (query.ascending() ? " ASC" : " DESC") + ", id DESC";
        List<JobItemView> rows = total == 0 ? List.of()
                : mapper.searchManaged(keyword, keywordId, status, scope.recruiterId(), scope.hiringManagerId(),
                orderBy, query.size(), offset(query.page(), query.size()));
        return PageResponse.of(rows.stream().map(MyBatisJobReadQuery::toRow).toList(), query.page(), query.size(),
                total);
    }

    @Override
    public JobStatsResponse countByStatus(JobScope scope) {
        Map<String, Long> counts = new LinkedHashMap<>();
        mapper.countByStatus(scope.recruiterId(), scope.hiringManagerId())
                .forEach(row -> counts.put(row.getStatus(), row.getTotal()));
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        return new JobStatsResponse(total,
                counts.getOrDefault(JobStatus.PUBLISHED.name(), 0L),
                counts.getOrDefault(JobStatus.DRAFT.name(), 0L),
                counts.getOrDefault(JobStatus.PENDING_APPROVAL.name(), 0L),
                counts.getOrDefault(JobStatus.CLOSED.name(), 0L));
    }

    @Override
    public PageResponse<JobRow> searchPublished(CareerJobQuery query, LocalDate today) {
        String keyword = LikePatterns.containsLower(query.keyword());
        String location = query.location() == null || query.location().isBlank() ? null : query.location().trim();
        String type = query.employmentType() == null ? null : query.employmentType().name();
        long total = mapper.countPublished(keyword, query.departmentId(), location, type, today);
        String orderBy = switch (query.sort()) {
            case MOST_RECENT -> "published_at DESC, id DESC";
            case OLDEST -> "published_at ASC, id ASC";
            case TITLE_ASC -> "LOWER(title) ASC, id ASC";
            case TITLE_DESC -> "LOWER(title) DESC, id DESC";
        };
        List<JobItemView> rows = total == 0 ? List.of()
                : mapper.searchPublished(keyword, query.departmentId(), location, type, today, orderBy,
                query.size(), offset(query.page(), query.size()));
        return PageResponse.of(rows.stream().map(MyBatisJobReadQuery::toRow).toList(), query.page(), query.size(),
                total);
    }

    @Override
    public Map<Long, List<SkillInfo>> findSkillsByJobIds(Collection<Long> jobIds) {
        if (jobIds.isEmpty()) {
            return Map.of();
        }
        return mapper.findSkillsByJobIds(jobIds).stream()
                .collect(Collectors.groupingBy(JobSkillView::getJobId, LinkedHashMap::new,
                        Collectors.mapping(view -> new SkillInfo(view.getSkillId(), view.getSkillName(),
                                view.getCategory()), Collectors.toList())));
    }

    @Override
    public List<Long> findDepartmentIdsOfPublishedJobs(LocalDate today) {
        return mapper.findDepartmentIdsOfPublished(today);
    }

    @Override
    public List<String> findLocationsOfPublishedJobs(LocalDate today) {
        return mapper.findLocationsOfPublished(today);
    }

    @Override
    public Set<Long> findJobIdsAssignedTo(Long userId) {
        return new HashSet<>(mapper.findJobIdsAssignedTo(userId));
    }

    private static long offset(int page, int size) {
        return (page - 1L) * size;
    }

    private static Long parseId(String keyword) {
        if (keyword == null) {
            return null;
        }
        try {
            return Long.parseLong(keyword.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static JobRow toRow(JobItemView view) {
        return new JobRow(view.getId(), view.getTitle(), view.getDepartmentId(), view.getEmploymentType(),
                view.getLocation(), view.getDescription(), view.getSalaryMin(), view.getSalaryMax(),
                view.getSalaryCurrency(), view.getStatus(), view.getPriority(), view.getRecruiterId(),
                view.getHiringManagerId(), view.getDeadline(), view.getPublishedAt(), view.getCreatedAt());
    }
}
