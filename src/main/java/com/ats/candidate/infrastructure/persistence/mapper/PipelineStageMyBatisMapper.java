package com.ats.candidate.infrastructure.persistence.mapper;

import com.ats.candidate.infrastructure.persistence.record.PipelineStageRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PipelineStageMyBatisMapper {

    List<PipelineStageRecord> findAll();

    Optional<PipelineStageRecord> findById(@Param("id") Long id);

    int update(PipelineStageRecord record);
}
