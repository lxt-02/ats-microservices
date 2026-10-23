package com.ats.job.domain.repository;

import com.ats.job.domain.aggregate.Job;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Persists a job together with its required skills. Finders ignore soft-deleted jobs. */
public interface JobRepository {

    Job save(Job job);

    Optional<Job> findById(Long id);

    List<Job> findByIds(Collection<Long> ids);

    /** Published jobs whose deadline is on or before {@code date}. */
    List<Job> findPublishedWithDeadlineOnOrBefore(LocalDate date);
}
