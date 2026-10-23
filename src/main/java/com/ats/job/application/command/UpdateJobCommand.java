package com.ats.job.application.command;

public record UpdateJobCommand(Long id, JobFormCommand form) {
}
