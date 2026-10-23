package com.ats.candidate.infrastructure.persistence.mapper;

import com.ats.candidate.infrastructure.persistence.record.CvRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CvMyBatisMapper {

    int insert(CvRecord record);

    int update(CvRecord record);

    Optional<CvRecord> findActiveById(@Param("id") Long id);

    List<CvRecord> findActiveByCandidateId(@Param("candidateId") Long candidateId);
}
