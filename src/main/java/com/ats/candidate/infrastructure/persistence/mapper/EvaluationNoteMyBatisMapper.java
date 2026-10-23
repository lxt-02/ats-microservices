package com.ats.candidate.infrastructure.persistence.mapper;

import com.ats.candidate.infrastructure.persistence.record.EvaluationNoteRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EvaluationNoteMyBatisMapper {

    int insert(EvaluationNoteRecord record);

    List<EvaluationNoteRecord> findActiveByApplicationId(@Param("applicationId") Long applicationId);
}
