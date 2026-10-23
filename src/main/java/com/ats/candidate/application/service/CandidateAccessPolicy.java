package com.ats.candidate.application.service;

import com.ats.candidate.application.port.out.CandidateReadQuery;
import com.ats.candidate.application.query.CandidateVisibility;
import com.ats.job.application.contract.JobDirectory;
import com.ats.job.application.contract.JobInfo;
import com.ats.shared.exception.ResourceNotFoundException;
import com.ats.shared.security.CurrentUser;
import com.ats.shared.security.Roles;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * BRULE-01 Job-based data isolation: a recruiter only sees candidates who applied to their jobs (or that they
 * imported themselves); a hiring manager only those of the jobs they manage; an admin sees everyone.
 */
@Component
public class CandidateAccessPolicy {

    private final JobDirectory jobDirectory;
    private final CandidateReadQuery candidateReadQuery;

    public CandidateAccessPolicy(JobDirectory jobDirectory, CandidateReadQuery candidateReadQuery) {
        this.jobDirectory = jobDirectory;
        this.candidateReadQuery = candidateReadQuery;
    }

    public CandidateVisibility visibilityFor(CurrentUser user) {
        if (user.hasRole(Roles.ADMIN)) {
            return CandidateVisibility.everything();
        }
        if (user.hasRole(Roles.RECRUITER) || user.hasRole(Roles.HIRING_MANAGER)) {
            String importer = user.hasRole(Roles.RECRUITER) ? user.actor() : null;
            return new CandidateVisibility(false, importer, jobDirectory.findJobIdsAssignedTo(user.userId()));
        }
        throw new AccessDeniedException("You do not have access to candidates");
    }

    /** 404 rather than 403, so the existence of other recruiters' candidates is not revealed. */
    public void ensureCanView(Long candidateId, CurrentUser user) {
        if (!candidateReadQuery.isVisible(candidateId, visibilityFor(user))) {
            throw new ResourceNotFoundException("Candidate not found: " + candidateId);
        }
    }

    /** The job must exist and be assigned to the user (recruiter or hiring manager), unless admin. */
    public JobInfo ensureCanAccessJob(Long jobId, CurrentUser user) {
        JobInfo job = jobDirectory.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));
        boolean assigned = Objects.equals(job.recruiterId(), user.userId())
                || Objects.equals(job.hiringManagerId(), user.userId());
        if (!user.hasRole(Roles.ADMIN) && !assigned) {
            throw new AccessDeniedException("You do not have access to this job");
        }
        return job;
    }

    /** Changing the pipeline of a job: its recruiter, or an admin. */
    public JobInfo ensureCanManageJob(Long jobId, CurrentUser user) {
        JobInfo job = ensureCanAccessJob(jobId, user);
        if (!user.hasRole(Roles.ADMIN) && !(user.hasRole(Roles.RECRUITER)
                && Objects.equals(job.recruiterId(), user.userId()))) {
            throw new AccessDeniedException("Only the recruiter of this job can change its pipeline");
        }
        return job;
    }

    /** BRULE-05: only the hiring manager of the job approves, rejects and assigns after the HM interview. */
    public JobInfo ensureIsHiringManagerOf(Long jobId, CurrentUser user) {
        JobInfo job = ensureCanAccessJob(jobId, user);
        if (!user.hasRole(Roles.HIRING_MANAGER) || !Objects.equals(job.hiringManagerId(), user.userId())) {
            throw new AccessDeniedException("Only the hiring manager of this job can make this decision");
        }
        return job;
    }

    public boolean canManageJob(JobInfo job, CurrentUser user) {
        return user.hasRole(Roles.ADMIN)
                || (user.hasRole(Roles.RECRUITER) && Objects.equals(job.recruiterId(), user.userId()));
    }

    public boolean isHiringManagerOf(JobInfo job, CurrentUser user) {
        return user.hasRole(Roles.HIRING_MANAGER) && Objects.equals(job.hiringManagerId(), user.userId());
    }
}
