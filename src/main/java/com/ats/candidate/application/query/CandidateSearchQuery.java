package com.ats.candidate.application.query;

import com.ats.candidate.domain.enums.CvParseStatus;

import java.util.Set;

/**
 * UC-02.5 Search & Filter. {@code keyword} matches name, email, phone, CV text or a skill name;
 * {@code skillIds}: the candidate must have all of them. {@code sortKey}: createdAt | fullName.
 */
public record CandidateSearchQuery(
        String keyword,
        Set<Long> skillIds,
        Long jobId,
        String source,
        CvParseStatus parseStatus,
        Boolean duplicate,
        String sortKey,
        boolean ascending,
        int page,
        int size
) {

    public CandidateSearchQuery {
        skillIds = skillIds == null ? Set.of() : Set.copyOf(skillIds);
    }
}
