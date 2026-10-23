package com.ats.candidate.infrastructure.persistence.mapper;

import com.ats.candidate.infrastructure.persistence.record.JobApplicationRecord;
import com.ats.candidate.infrastructure.persistence.record.StageTransitionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface JobApplicationMyBatisMapper {

    int insert(JobApplicationRecord record);

    int update(JobApplicationRecord record);

    Optional<JobApplicationRecord> findActiveById(@Param("id") Long id);

    boolean existsActive(@Param("jobId") Long jobId, @Param("candidateId") Long candidateId);

    int insertTransition(StageTransitionRecord record);

    List<StageTransitionRecord> findTransitions(@Param("applicationId") Long applicationId);
}
