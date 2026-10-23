package com.ats.job.application.command;

/** {@code recruiterId} is only used when an admin creates the job; a recruiter is always assigned to themself. */
public record CreateJobCommand(JobFormCommand form, Long recruiterId) {
}
