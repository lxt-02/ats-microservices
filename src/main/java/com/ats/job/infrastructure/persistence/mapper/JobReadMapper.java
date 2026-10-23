package com.ats.job.infrastructure.persistence.mapper;

import com.ats.job.infrastructure.persistence.view.JobItemView;
import com.ats.job.infrastructure.persistence.view.JobSkillView;
import com.ats.job.infrastructure.persistence.view.StatusCountView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/** Screen queries; parameters are prepared (escaped patterns, whitelisted ORDER BY) by the caller. */
@Mapper
public interface JobReadMapper {

    long countManaged(@Param("keyword") String keyword, @Param("keywordId") Long keywordId,
                      @Param("status") String status, @Param("recruiterId") Long recruiterId,
                      @Param("hiringManagerId") Long hiringManagerId);

    List<JobItemView> searchManaged(@Param("keyword") String keyword, @Param("keywordId") Long keywordId,
                                    @Param("status") String status, @Param("recruiterId") Long recruiterId,
                                    @Param("hiringManagerId") Long hiringManagerId,
                                    @Param("orderBy") String orderBy, @Param("limit") int limit,
                                    @Param("offset") long offset);

    List<StatusCountView> countByStatus(@Param("recruiterId") Long recruiterId,
                                        @Param("hiringManagerId") Long hiringManagerId);

    long countPublished(@Param("keyword") String keyword, @Param("departmentId") Long departmentId,
                       @Param("location") String location, @Param("employmentType") String employmentType,
                       @Param("today") LocalDate today);

    List<JobItemView> searchPublished(@Param("keyword") String keyword, @Param("departmentId") Long departmentId,
                                      @Param("location") String location,
                                      @Param("employmentType") String employmentType,
                                      @Param("today") LocalDate today, @Param("orderBy") String orderBy,
                                      @Param("limit") int limit, @Param("offset") long offset);

    List<JobSkillView> findSkillsByJobIds(@Param("jobIds") Collection<Long> jobIds);

    List<Long> findDepartmentIdsOfPublished(@Param("today") LocalDate today);

    List<String> findLocationsOfPublished(@Param("today") LocalDate today);

    List<Long> findJobIdsAssignedTo(@Param("userId") Long userId);
}
