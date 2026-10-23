package com.ats.candidate.infrastructure.persistence.mapper;

import com.ats.candidate.infrastructure.persistence.record.JobApplicationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface JobApplicationMyBatisMapper {

    int insert(JobApplicationRecord record);

    int update(JobApplicationRecord record);

    boolean existsActive(@Param("jobId") Long jobId, @Param("candidateId") Long candidateId);
}
