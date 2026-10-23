package com.ats.job.infrastructure.persistence.mapper;

import com.ats.job.infrastructure.persistence.record.SkillRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Mapper
public interface SkillMyBatisMapper {

    int insert(SkillRecord record);

    Optional<SkillRecord> findActiveByLowerName(@Param("name") String lowerName);

    List<SkillRecord> findAllActive();

    List<SkillRecord> findActiveByIds(@Param("ids") Collection<Long> ids);
}
