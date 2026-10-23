package com.ats.job.infrastructure.job;

import com.ats.job.application.usecase.CloseExpiredJobsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * BRULE-10: closes published jobs at 23:59 on their deadline date (Vietnam time). Also runs every hour so a
 * missed run (application down at 23:59) is caught up; the Career Site already hides expired jobs meanwhile.
 */
@Component
public class CloseExpiredJobsScheduler {

    private static final Logger log = LoggerFactory.getLogger(CloseExpiredJobsScheduler.class);

    private final CloseExpiredJobsUseCase closeExpiredJobsUseCase;

    public CloseExpiredJobsScheduler(CloseExpiredJobsUseCase closeExpiredJobsUseCase) {
        this.closeExpiredJobsUseCase = closeExpiredJobsUseCase;
    }

    @Scheduled(cron = "${ats.jobs.auto-close-cron:0 59 * * * *}", zone = "Asia/Ho_Chi_Minh")
    public void closeExpiredJobs() {
        int closed = closeExpiredJobsUseCase.execute();
        if (closed > 0) {
            log.info("Closed {} job(s) whose application deadline has passed", closed);
        }
    }
}
