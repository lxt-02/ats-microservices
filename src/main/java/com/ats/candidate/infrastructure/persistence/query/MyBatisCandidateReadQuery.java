package com.ats.candidate.infrastructure.persistence.query;

import com.ats.candidate.application.port.out.CandidateReadQuery;
import com.ats.candidate.application.query.CandidateSearchQuery;
import com.ats.candidate.application.query.CandidateVisibility;
import com.ats.candidate.infrastructure.persistence.mapper.CandidateMyBatisMapper;
import com.ats.candidate.infrastructure.persistence.mapper.CandidateReadMapper;
import com.ats.candidate.infrastructure.persistence.view.ApplicationItemView;
import com.ats.candidate.infrastructure.persistence.view.CandidateItemView;
import com.ats.candidate.infrastructure.persistence.view.IdPairView;
import com.ats.shared.infrastructure.mybatis.LikePatterns;
import com.ats.shared.web.PageResponse;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class MyBatisCandidateReadQuery implements CandidateReadQuery {

    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "createdAt", "c.created_at",
            "fullName", "LOWER(c.full_name)");

    private final CandidateReadMapper mapper;
    private final CandidateMyBatisMapper candidateMapper;

    public MyBatisCandidateReadQuery(CandidateReadMapper mapper, CandidateMyBatisMapper candidateMapper) {
        this.mapper = mapper;
        this.candidateMapper = candidateMapper;
    }

    @Override
    public PageResponse<CandidateRow> search(CandidateSearchQuery query, Set<Long> keywordSkillIds,
                                             CandidateVisibility visibility) {
        Map<String, Object> filter = visibilityFilter(visibility);
        filter.put("keyword", LikePatterns.containsLower(query.keyword()));
        filter.put("keywordSkillIds", keywordSkillIds == null || keywordSkillIds.isEmpty() ? null : keywordSkillIds);
        filter.put("skillIds", query.skillIds().isEmpty() ? null : query.skillIds());
        filter.put("jobId", query.jobId());
        filter.put("source", query.source() == null || query.source().isBlank() ? null : query.source().trim());
        filter.put("parseStatus", query.parseStatus() == null ? null : query.parseStatus().name());
        filter.put("duplicate", query.duplicate());

        long total = mapper.countCandidates(filter);
        if (total == 0) {
            return PageResponse.of(List.of(), query.page(), query.size(), 0);
        }
        String column = query.sortKey() == null ? "c.created_at"
                : SORT_COLUMNS.getOrDefault(query.sortKey(), "c.created_at");
        filter.put("orderBy", column + (query.ascending() ? " ASC" : " DESC") + ", c.id DESC");
        filter.put("limit", query.size());
        filter.put("offset", (query.page() - 1L) * query.size());
        List<CandidateRow> rows = mapper.searchCandidates(filter).stream()
                .map(MyBatisCandidateReadQuery::toRow)
                .toList();
        return PageResponse.of(rows, query.page(), query.size(), total);
    }

    @Override
    public boolean isVisible(Long candidateId, CandidateVisibility visibility) {
        return mapper.isVisible(candidateId, visibilityFilter(visibility));
    }

    @Override
    public Map<Long, Set<Long>> findSkillIdsByCandidateIds(Collection<Long> candidateIds) {
        if (candidateIds.isEmpty()) {
            return Map.of();
        }
        return candidateMapper.findSkills(candidateIds).stream()
                .collect(Collectors.groupingBy(IdPairView::getKeyId,
                        Collectors.mapping(IdPairView::getValueId, Collectors.toSet())));
    }

    @Override
    public List<ApplicationRow> findApplicationsByCandidateId(Long candidateId) {
        return mapper.findApplications(candidateId, null, null, null).stream()
                .map(MyBatisCandidateReadQuery::toRow)
                .toList();
    }

    @Override
    public PageResponse<ApplicationRow> findApplicationsByJobId(Long jobId, int page, int size) {
        long total = mapper.countApplicationsOfJob(jobId);
        List<ApplicationRow> rows = total == 0 ? List.of()
                : mapper.findApplications(null, jobId, size, (page - 1L) * size).stream()
                .map(MyBatisCandidateReadQuery::toRow)
                .toList();
        return PageResponse.of(rows, page, size, total);
    }

    @Override
    public Map<Long, Long> countApplicationsByJobIds(Collection<Long> jobIds) {
        return mapper.countApplicationsByJobIds(jobIds).stream()
                .collect(Collectors.toMap(IdPairView::getKeyId, IdPairView::getValueId));
    }

    private static Map<String, Object> visibilityFilter(CandidateVisibility visibility) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("restricted", !visibility.all());
        filter.put("createdBy", visibility.createdBy());
        filter.put("jobIds", visibility.jobIds().isEmpty() ? null : visibility.jobIds());
        return filter;
    }

    private static CandidateRow toRow(CandidateItemView view) {
        return new CandidateRow(view.getId(), view.getFullName(), view.getEmail(), view.getPhone(), view.getSource(),
                view.isDuplicate(), view.getCreatedAt(), view.getLatestCvId(), view.getLatestCvFileName(),
                view.getLatestCvParseStatus(), view.getApplicationCount());
    }

    private static ApplicationRow toRow(ApplicationItemView view) {
        return new ApplicationRow(view.getId(), view.getJobId(), view.getCandidateId(), view.getCandidateName(),
                view.getCandidateEmail(), view.getCandidatePhone(), view.getStatus(), view.getSource(),
                view.getAppliedAt(), view.getCvId(), view.getCvParseStatus());
    }
}
