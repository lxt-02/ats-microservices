package com.ats.candidate.infrastructure.persistence.mapper;

import com.ats.candidate.infrastructure.persistence.view.AssignmentView;
import com.ats.candidate.infrastructure.persistence.view.BoardCardView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ApplicationReadMapper {

    List<BoardCardView> findBoardCards(@Param("jobId") Long jobId);

    /** {@code jobIds} null = every job. */
    List<AssignmentView> findApprovedAssignments(@Param("jobIds") Collection<Long> jobIds);
}
