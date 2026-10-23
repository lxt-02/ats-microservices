package com.ats.job.application.service;

import com.ats.job.application.contract.JobApplicationCounter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

/**
 * Placeholder until the Candidate module provides the real {@link JobApplicationCounter}: no job has applications.
 * Removed when the Candidate module is added.
 */
@Component
class NoApplicationsCounter implements JobApplicationCounter {

    @Override
    public Map<Long, Long> countByJobIds(Collection<Long> jobIds) {
        return Map.of();
    }
}
