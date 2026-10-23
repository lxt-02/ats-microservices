package com.ats.candidate.infrastructure.persistence.mapper;

import com.ats.candidate.infrastructure.persistence.view.ApplicationItemView;
import com.ats.candidate.infrastructure.persistence.view.CandidateItemView;
import com.ats.candidate.infrastructure.persistence.view.IdPairView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Screen queries. {@code filter} is a map prepared by the caller (escaped patterns, whitelisted ORDER BY). */
@Mapper
public interface CandidateReadMapper {

    long countCandidates(@Param("f") Map<String, Object> filter);

    List<CandidateItemView> searchCandidates(@Param("f") Map<String, Object> filter);

    boolean isVisible(@Param("candidateId") Long candidateId, @Param("f") Map<String, Object> visibility);

    List<ApplicationItemView> findApplications(@Param("candidateId") Long candidateId, @Param("jobId") Long jobId,
                                               @Param("limit") Integer limit, @Param("offset") Long offset);

    long countApplicationsOfJob(@Param("jobId") Long jobId);

    /** keyId = job id, valueId = number of applications. */
    List<IdPairView> countApplicationsByJobIds(@Param("jobIds") Collection<Long> jobIds);
}
