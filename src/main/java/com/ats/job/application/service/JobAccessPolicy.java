package com.ats.job.application.service;

import com.ats.job.application.query.JobScope;
import com.ats.job.domain.aggregate.Job;
import com.ats.shared.security.CurrentUser;
import com.ats.shared.security.Roles;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Data-level access to jobs (BRULE-01, BRULE-05): admins see everything, a recruiter works on the jobs assigned
 * to them, a hiring manager sees and reviews the jobs they manage.
 */
@Component
public class JobAccessPolicy {

    private static final String NO_ACCESS = "You do not have access to this job";

    public JobScope scopeFor(CurrentUser user) {
        if (user.hasRole(Roles.ADMIN)) {
            return JobScope.all();
        }
        if (user.hasRole(Roles.RECRUITER)) {
            return new JobScope(user.userId(), null);
        }
        if (user.hasRole(Roles.HIRING_MANAGER)) {
            return new JobScope(null, user.userId());
        }
        throw new AccessDeniedException(NO_ACCESS);
    }

    public void ensureCanView(Job job, CurrentUser user) {
        if (!(user.hasRole(Roles.ADMIN) || isRecruiterOf(job, user) || isHiringManagerOf(job, user))) {
            throw new AccessDeniedException(NO_ACCESS);
        }
    }

    public void ensureCanManage(Job job, CurrentUser user) {
        if (!(user.hasRole(Roles.ADMIN) || isRecruiterOf(job, user))) {
            throw new AccessDeniedException(NO_ACCESS);
        }
    }

    /** Only the hiring manager named on the requisition approves or rejects it. */
    public void ensureCanReview(Job job, CurrentUser user) {
        if (!isHiringManagerOf(job, user)) {
            throw new AccessDeniedException("Only the hiring manager of this job can approve or reject it");
        }
    }

    private static boolean isRecruiterOf(Job job, CurrentUser user) {
        return user.hasRole(Roles.RECRUITER) && job.isManagedBy(user.userId());
    }

    private static boolean isHiringManagerOf(Job job, CurrentUser user) {
        return user.hasRole(Roles.HIRING_MANAGER) && Objects.equals(job.getHiringManagerId(), user.userId());
    }
}
