package com.ats.job.infrastructure.persistence.mapper;

import com.ats.job.infrastructure.persistence.record.JobRecord;
import com.ats.job.infrastructure.persistence.view.JobSkillView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Mapper
public interface JobMyBatisMapper {

    int insert(JobRecord record);

    int update(JobRecord record);

    Optional<JobRecord> findActiveById(@Param("id") Long id);

    List<JobRecord> findActiveByIds(@Param("ids") Collection<Long> ids);

    List<JobRecord> findPublishedWithDeadlineOnOrBefore(@Param("date") LocalDate date);

    List<JobSkillView> findSkillIdsByJobIds(@Param("jobIds") Collection<Long> jobIds);

    int deleteSkills(@Param("jobId") Long jobId);

    int insertSkills(@Param("jobId") Long jobId, @Param("skillIds") Collection<Long> skillIds);
}
