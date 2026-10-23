package com.ats.candidate.infrastructure.persistence.repository;

import com.ats.candidate.domain.aggregate.Cv;
import com.ats.candidate.domain.repository.CvRepository;
import com.ats.candidate.infrastructure.persistence.converter.CvPersistenceConverter;
import com.ats.candidate.infrastructure.persistence.mapper.CvMyBatisMapper;
import com.ats.candidate.infrastructure.persistence.record.CvRecord;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MyBatisCvRepository implements CvRepository {

    private final CvMyBatisMapper mapper;

    public MyBatisCvRepository(CvMyBatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Cv save(Cv cv) {
        CvRecord record = CvPersistenceConverter.toRecord(cv);
        if (record.getId() == null) {
            mapper.insert(record);
        } else {
            mapper.update(record);
        }
        return CvPersistenceConverter.toAggregate(record);
    }

    @Override
    public Optional<Cv> findById(Long id) {
        return mapper.findActiveById(id).map(CvPersistenceConverter::toAggregate);
    }

    @Override
    public List<Cv> findByCandidateId(Long candidateId) {
        return mapper.findActiveByCandidateId(candidateId).stream().map(CvPersistenceConverter::toAggregate).toList();
    }
}
