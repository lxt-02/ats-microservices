package com.ats.candidate.infrastructure.persistence.repository;

import com.ats.candidate.domain.aggregate.Candidate;
import com.ats.candidate.domain.repository.CandidateRepository;
import com.ats.candidate.infrastructure.persistence.converter.CandidatePersistenceConverter;
import com.ats.candidate.infrastructure.persistence.mapper.CandidateMyBatisMapper;
import com.ats.candidate.infrastructure.persistence.record.CandidateRecord;
import com.ats.candidate.infrastructure.persistence.view.IdPairView;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class MyBatisCandidateRepository implements CandidateRepository {

    private final CandidateMyBatisMapper mapper;

    public MyBatisCandidateRepository(CandidateMyBatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Candidate save(Candidate candidate) {
        CandidateRecord record = CandidatePersistenceConverter.toRecord(candidate);
        if (record.getId() == null) {
            mapper.insert(record);
        } else {
            mapper.update(record);
            mapper.deleteSkills(record.getId());
        }
        if (!candidate.getSkillIds().isEmpty()) {
            mapper.insertSkills(record.getId(), candidate.getSkillIds());
        }
        return CandidatePersistenceConverter.toAggregate(record, candidate.getSkillIds());
    }

    @Override
    public Optional<Candidate> findById(Long id) {
        return mapper.findActiveById(id).map(this::toAggregate);
    }

    @Override
    public Optional<Candidate> findByEmail(String normalizedEmail) {
        return mapper.findActiveByEmail(normalizedEmail).map(this::toAggregate);
    }

    @Override
    public Optional<Long> findOtherIdByEmailOrPhone(Long excludeId, String normalizedEmail, String phone) {
        String phoneKey = CandidatePersistenceConverter.phoneKey(phone);
        if (normalizedEmail == null && phoneKey == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapper.findOtherIdByEmailOrPhone(excludeId, normalizedEmail, phoneKey));
    }

    private Candidate toAggregate(CandidateRecord record) {
        Set<Long> skills = mapper.findSkills(List.of(record.getId())).stream()
                .map(IdPairView::getValueId)
                .collect(Collectors.toSet());
        return CandidatePersistenceConverter.toAggregate(record, skills);
    }
}
