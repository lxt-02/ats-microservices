package com.ats.job.application.contract;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** What other modules may ask about jobs. Deleted jobs are never returned. */
public interface JobDirectory {

    Optional<JobInfo> findById(Long jobId);

    Map<Long, JobInfo> findByIds(Collection<Long> jobIds);

    /** Jobs where the user is the assigned recruiter or the hiring manager (BRULE-01 / BRULE-05 scope). */
    Set<Long> findJobIdsAssignedTo(Long userId);
}
