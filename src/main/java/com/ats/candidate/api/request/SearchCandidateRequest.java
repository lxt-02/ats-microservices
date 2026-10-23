package com.ats.candidate.api.request;

import com.ats.candidate.application.query.CandidateSearchQuery;
import com.ats.candidate.domain.enums.CvParseStatus;
import com.ats.shared.validation.AllowedSortKeys;
import com.ats.shared.web.BasePagination;
import jakarta.validation.constraints.Size;

import java.util.Set;

/** {@code ?keyword=java&skillIds=1,2&jobId=3&source=LINKEDIN&parseStatus=PARTIAL&page=1&size=20&sort=createdAt,desc}. */
public record SearchCandidateRequest(
        @Size(max = 200) String keyword,
        Set<Long> skillIds,
        Long jobId,
        @Size(max = 150) String source,
        CvParseStatus parseStatus,
        Boolean duplicate,
        Integer page,
        Integer size,
        @AllowedSortKeys({"createdAt", "fullName"}) String sort
) implements BasePagination {

    public CandidateSearchQuery toQuery() {
        return new CandidateSearchQuery(keyword, skillIds, jobId, source, parseStatus, duplicate, sortKey(),
                sortAscending(), pageOrDefault(), sizeOrDefault());
    }
}
