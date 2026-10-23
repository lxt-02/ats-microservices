package com.ats.candidate.application.usecase;

import com.ats.candidate.application.port.out.CandidateReadQuery;
import com.ats.candidate.application.port.out.CandidateReadQuery.CandidateRow;
import com.ats.candidate.application.query.CandidateSearchQuery;
import com.ats.candidate.application.response.CandidateListItemResponse;
import com.ats.candidate.application.response.CvSummary;
import com.ats.candidate.application.service.CandidateAccessPolicy;
import com.ats.job.application.contract.SkillCatalog;
import com.ats.job.application.contract.SkillInfo;
import com.ats.shared.security.CurrentUser;
import com.ats.shared.web.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** UC-02.5 Search & Filter Candidates, within the user's visibility (BRULE-01). */
@Service
public class SearchCandidatesUseCase {

    private final CandidateReadQuery candidateReadQuery;
    private final CandidateAccessPolicy accessPolicy;
    private final SkillCatalog skillCatalog;

    public SearchCandidatesUseCase(CandidateReadQuery candidateReadQuery, CandidateAccessPolicy accessPolicy,
                                   SkillCatalog skillCatalog) {
        this.candidateReadQuery = candidateReadQuery;
        this.accessPolicy = accessPolicy;
        this.skillCatalog = skillCatalog;
    }

    @Transactional(readOnly = true)
    public PageResponse<CandidateListItemResponse> execute(CandidateSearchQuery query, CurrentUser user) {
        Set<Long> keywordSkills = skillCatalog.findIdsByNameContaining(query.keyword());
        PageResponse<CandidateRow> page = candidateReadQuery.search(query, keywordSkills,
                accessPolicy.visibilityFor(user));

        Map<Long, Set<Long>> skillIds = candidateReadQuery.findSkillIdsByCandidateIds(
                page.items().stream().map(CandidateRow::id).toList());
        Map<Long, SkillInfo> skills = skillCatalog.findByIds(
                skillIds.values().stream().flatMap(Collection::stream).distinct().toList());
        return page.map(row -> new CandidateListItemResponse(
                row.id(), row.fullName(), row.email(), row.phone(), row.source(), row.duplicate(),
                skillIds.getOrDefault(row.id(), Set.of()).stream()
                        .map(skills::get).filter(Objects::nonNull).map(SkillInfo::name)
                        .sorted(String.CASE_INSENSITIVE_ORDER).toList(),
                row.latestCvId() == null ? null
                        : new CvSummary(row.latestCvId(), row.latestCvFileName(), row.latestCvParseStatus()),
                row.applicationCount(), row.createdAt()));
    }
}
