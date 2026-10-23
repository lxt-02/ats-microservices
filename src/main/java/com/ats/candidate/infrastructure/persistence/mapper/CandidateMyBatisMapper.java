package com.ats.candidate.infrastructure.persistence.mapper;

import com.ats.candidate.infrastructure.persistence.record.CandidateRecord;
import com.ats.candidate.infrastructure.persistence.view.IdPairView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Mapper
public interface CandidateMyBatisMapper {

    int insert(CandidateRecord record);

    int update(CandidateRecord record);

    Optional<CandidateRecord> findActiveById(@Param("id") Long id);

    Optional<CandidateRecord> findActiveByEmail(@Param("email") String email);

    Long findOtherIdByEmailOrPhone(@Param("excludeId") Long excludeId, @Param("email") String email,
                                   @Param("phoneKey") String phoneKey);

    /** keyId = candidate id, valueId = skill id. */
    List<IdPairView> findSkills(@Param("candidateIds") Collection<Long> candidateIds);

    int deleteSkills(@Param("candidateId") Long candidateId);

    int insertSkills(@Param("candidateId") Long candidateId, @Param("skillIds") Collection<Long> skillIds);
}
