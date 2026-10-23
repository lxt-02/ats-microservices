package com.ats.candidate.domain.repository;

import com.ats.candidate.domain.aggregate.JobApplication;
import com.ats.candidate.domain.valueobject.StageTransition;

import java.util.List;
import java.util.Optional;

/** Saves an application together with its new stage transitions (audit trail). */
public interface JobApplicationRepository {

    /** Throws Spring's DuplicateKeyException when the candidate already applied to the job. */
    JobApplication save(JobApplication application);

    Optional<JobApplication> findById(Long id);

    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);

    /** Oldest first. */
    List<StageTransition> findTransitions(Long applicationId);
}
