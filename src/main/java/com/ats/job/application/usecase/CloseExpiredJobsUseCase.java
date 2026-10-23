package com.ats.job.application.usecase;

import com.ats.job.application.service.BusinessClock;
import com.ats.job.domain.aggregate.Job;
import com.ats.job.domain.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

/** BRULE-10: published jobs are closed at the end of their deadline date. Returns how many were closed. */
@Service
public class CloseExpiredJobsUseCase {

    private static final String SYSTEM_ACTOR = "system";

    private final JobRepository jobRepository;
    private final BusinessClock clock;

    public CloseExpiredJobsUseCase(JobRepository jobRepository, BusinessClock clock) {
        this.jobRepository = jobRepository;
        this.clock = clock;
    }

    @Transactional
    public int execute() {
        LocalDateTime businessNow = clock.localNow();
        Instant now = clock.now();
        int closed = 0;
        for (Job job : jobRepository.findPublishedWithDeadlineOnOrBefore(businessNow.toLocalDate())) {
            if (job.closeIfExpired(businessNow, SYSTEM_ACTOR, now)) {
                jobRepository.save(job);
                closed++;
            }
        }
        return closed;
    }
}
