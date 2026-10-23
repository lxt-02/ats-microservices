package com.ats.candidate.application.port.out;

import com.ats.candidate.application.query.CandidateSearchQuery;
import com.ats.candidate.application.query.CandidateVisibility;
import com.ats.shared.web.PageResponse;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Screen queries of the Candidate module. */
public interface CandidateReadQuery {

    /** @param keywordSkillIds skills whose name matches the keyword (resolved by the caller) */
    PageResponse<CandidateRow> search(CandidateSearchQuery query, Set<Long> keywordSkillIds,
                                      CandidateVisibility visibility);

    boolean isVisible(Long candidateId, CandidateVisibility visibility);

    Map<Long, Set<Long>> findSkillIdsByCandidateIds(Collection<Long> candidateIds);

    List<ApplicationRow> findApplicationsByCandidateId(Long candidateId);

    PageResponse<ApplicationRow> findApplicationsByJobId(Long jobId, int page, int size);

    Map<Long, Long> countApplicationsByJobIds(Collection<Long> jobIds);

    record CandidateRow(
            Long id,
            String fullName,
            String email,
            String phone,
            String source,
            boolean duplicate,
            Instant createdAt,
            Long latestCvId,
            String latestCvFileName,
            String latestCvParseStatus,
            long applicationCount
    ) {
    }

    record ApplicationRow(
            Long id,
            Long jobId,
            Long candidateId,
            String candidateName,
            String candidateEmail,
            String candidatePhone,
            String status,
            String source,
            Instant appliedAt,
            Long cvId,
            String cvParseStatus
    ) {
    }
}
