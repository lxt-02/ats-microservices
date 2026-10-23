package com.ats.job.application.contract;

import java.util.Collection;
import java.util.Map;

/**
 * Implemented by the module that owns applications (Candidate), so the Job module can show application counts
 * and refuse to delete jobs that have applications, without depending on that module.
 */
public interface JobApplicationCounter {

    /** Number of applications per job id; jobs without applications may be absent. */
    Map<Long, Long> countByJobIds(Collection<Long> jobIds);
}
