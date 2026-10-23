package com.ats.candidate.infrastructure.persistence.repository;

import com.ats.candidate.domain.aggregate.JobApplication;
import com.ats.candidate.domain.repository.JobApplicationRepository;
import com.ats.candidate.infrastructure.persistence.converter.JobApplicationPersistenceConverter;
import com.ats.candidate.infrastructure.persistence.mapper.JobApplicationMyBatisMapper;
import com.ats.candidate.infrastructure.persistence.record.JobApplicationRecord;
import org.springframework.stereotype.Repository;

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
        return JobApplicationPersistenceConverter.toAggregate(record);
    }

    @Override
    public boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId) {
        return mapper.existsActive(jobId, candidateId);
    }
}
