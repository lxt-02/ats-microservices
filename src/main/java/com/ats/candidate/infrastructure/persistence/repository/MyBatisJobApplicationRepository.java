package com.ats.candidate.infrastructure.persistence.repository;

import com.ats.candidate.domain.aggregate.JobApplication;
import com.ats.candidate.domain.repository.JobApplicationRepository;
import com.ats.candidate.domain.valueobject.StageTransition;
import com.ats.candidate.infrastructure.persistence.converter.JobApplicationPersistenceConverter;
import com.ats.candidate.infrastructure.persistence.mapper.JobApplicationMyBatisMapper;
import com.ats.candidate.infrastructure.persistence.record.JobApplicationRecord;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MyBatisJobApplicationRepository implements JobApplicationRepository {

    private final JobApplicationMyBatisMapper mapper;

    public MyBatisJobApplicationRepository(JobApplicationMyBatisMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public JobApplication save(JobApplication application) {
        JobApplicationRecord record = JobApplicationPersistenceConverter.toRecord(application);
        if (record.getId() == null) {
            mapper.insert(record);
        } else {
            mapper.update(record);
        }
        for (StageTransition transition : application.getNewTransitions()) {
            mapper.insertTransition(JobApplicationPersistenceConverter.toRecord(record.getId(), transition));
        }
        application.clearNewTransitions();
        return JobApplicationPersistenceConverter.toAggregate(record);
    }

    @Override
    public Optional<JobApplication> findById(Long id) {
        return mapper.findActiveById(id).map(JobApplicationPersistenceConverter::toAggregate);
    }

    @Override
    public boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId) {
        return mapper.existsActive(jobId, candidateId);
    }

    @Override
    public List<StageTransition> findTransitions(Long applicationId) {
        return mapper.findTransitions(applicationId).stream()
                .map(JobApplicationPersistenceConverter::toTransition)
                .toList();
    }
}
