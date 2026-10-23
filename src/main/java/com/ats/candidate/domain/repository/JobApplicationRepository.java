package com.ats.candidate.domain.repository;

import com.ats.candidate.domain.aggregate.JobApplication;

public interface JobApplicationRepository {

    /** @throws org.springframework.dao.DuplicateKeyException when the candidate already applied to the job */
    JobApplication save(JobApplication application);

    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);
}
