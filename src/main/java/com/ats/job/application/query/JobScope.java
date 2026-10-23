package com.ats.job.application.query;

/** Which jobs a user may see: all (admin), a recruiter's own jobs, or a hiring manager's jobs. */
public record JobScope(Long recruiterId, Long hiringManagerId) {

    public static JobScope all() {
        return new JobScope(null, null);
    }
}
