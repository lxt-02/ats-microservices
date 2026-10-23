package com.ats.candidate.application.service;

import com.ats.candidate.application.port.out.CandidateReadQuery;
import com.ats.job.application.contract.JobApplicationCounter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;

/** Gives the Job module its application counts. */
@Service
public class JobApplicationCounterService implements JobApplicationCounter {

    private final CandidateReadQuery candidateReadQuery;

    public JobApplicationCounterService(CandidateReadQuery candidateReadQuery) {
        this.candidateReadQuery = candidateReadQuery;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> countByJobIds(Collection<Long> jobIds) {
        return jobIds == null || jobIds.isEmpty() ? Map.of() : candidateReadQuery.countApplicationsByJobIds(jobIds);
    }
}
